package com.mortex.amromovies.core.data

import com.mortex.amromovies.core.common.time.AppClock
import com.mortex.amromovies.core.data.mapper.toCached
import com.mortex.amromovies.core.data.mapper.toDomain
import com.mortex.amromovies.core.data.source.MoviesLocalDataSource
import com.mortex.amromovies.core.data.source.MoviesRemoteDataSource
import com.mortex.amromovies.core.data.source.RemoteDataException
import com.mortex.amromovies.core.data.source.model.RemoteGenre
import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails
import com.mortex.amromovies.core.domain.repository.MoviesRepository
import com.mortex.amromovies.core.domain.result.AppError
import com.mortex.amromovies.core.domain.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class DefaultMoviesRepository(
    private val remoteDataSource: MoviesRemoteDataSource,
    private val localDataSource: MoviesLocalDataSource,
    private val clock: AppClock,
) : MoviesRepository {
    private val refreshMutex = Mutex()

    override fun observeWeeklyTrendingMovies(): Flow<List<Movie>> =
        localDataSource.observeWeeklyTrendingMovies().map { movies ->
            movies.map { movie -> movie.toDomain() }
        }

    override suspend fun refreshWeeklyTrendingMovies(force: Boolean): AppResult<Unit> = executeSafely {
        val timestampObservedBeforeLock = localDataSource.getWeeklyTrendingUpdatedAt()
        refreshMutex.withLock {
            val currentTimestamp = localDataSource.getWeeklyTrendingUpdatedAt()
            val anotherRefreshCompleted = currentTimestamp != timestampObservedBeforeLock
            if (anotherRefreshCompleted || (!force && currentTimestamp.isFresh())) {
                return@withLock
            }

            val movies = fetchWeeklyTrendingMovies()
            localDataSource.replaceWeeklyTrendingMovies(
                movies = movies.map(Movie::toCached),
                refreshedAtEpochMillis = clock.nowEpochMillis(),
            )
        }
    }

    private suspend fun fetchWeeklyTrendingMovies(): List<Movie> {
        val genres = remoteDataSource.getGenres()
        val genresById = genres.associateBy(RemoteGenre::id)
        val firstPage = remoteDataSource.getWeeklyTrendingMoviesPage(
            page = FIRST_PAGE,
        )
        val moviesById = LinkedHashMap<Long, Movie>(MAX_TRENDING_MOVIES)
        firstPage.movies.forEach { movie ->
            val domainMovie = movie.toDomain(genresById)
            moviesById.putIfAbsent(domainMovie.id, domainMovie)
        }

        var nextPage = FIRST_PAGE + 1
        while (moviesById.size < MAX_TRENDING_MOVIES && nextPage <= firstPage.totalPages) {
            val batchLastPage = minOf(
                nextPage + PAGE_BATCH_SIZE - 1,
                firstPage.totalPages,
            )
            val pages = coroutineScope {
                (nextPage..batchLastPage)
                    .map { page ->
                        async {
                            remoteDataSource.getWeeklyTrendingMoviesPage(
                                page = page,
                            )
                        }
                    }
                    .awaitAll()
            }

            pages.forEach { page ->
                page.movies.forEach { movie ->
                    val domainMovie = movie.toDomain(genresById)
                    moviesById.putIfAbsent(domainMovie.id, domainMovie)
                }
            }
            nextPage = batchLastPage + 1
        }

        return moviesById.values.take(MAX_TRENDING_MOVIES)
    }

    override suspend fun getGenres(): AppResult<List<Genre>> = executeSafely {
        remoteDataSource.getGenres().map(RemoteGenre::toDomain)
    }

    override suspend fun getMovieDetails(movieId: Long): AppResult<MovieDetails> = executeSafely {
        remoteDataSource.getMovieDetails(movieId).toDomain()
    }

    private suspend fun <T> executeSafely(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (remote: RemoteDataException) {
        AppResult.Error(remote.toAppError())
    } catch (unexpected: Exception) {
        AppResult.Error(AppError.Unknown(unexpected))
    }

    private fun RemoteDataException.toAppError(): AppError = when (this) {
        is RemoteDataException.NoInternet -> AppError.NoInternet
        RemoteDataException.Unauthorized -> AppError.Unauthorized
        RemoteDataException.NotFound -> AppError.NotFound
        is RemoteDataException.Server -> AppError.Server(statusCode)
        is RemoteDataException.InvalidResponse -> AppError.Unknown(cause)
    }

    private fun Long?.isFresh(): Boolean =
        this != null && this >= clock.nowEpochMillis() - CACHE_MAX_AGE_MILLIS

    private companion object {
        const val FIRST_PAGE = 1
        const val MAX_TRENDING_MOVIES = 100
        const val PAGE_BATCH_SIZE = 4
        const val CACHE_MAX_AGE_MILLIS = 60 * 60 * 1_000L
    }
}
