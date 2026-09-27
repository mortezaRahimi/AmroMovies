package com.mortex.amromovies.core.data

import com.mortex.amromovies.core.common.time.AppClock
import com.mortex.amromovies.core.database.MoviesLocalDataSource
import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails
import com.mortex.amromovies.core.domain.repository.MoviesRepository
import com.mortex.amromovies.core.domain.result.AppError
import com.mortex.amromovies.core.domain.result.AppResult
import com.mortex.amromovies.core.network.TmdbRemoteDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

class DefaultMoviesRepository(
    private val remoteDataSource: TmdbRemoteDataSource,
    private val localDataSource: MoviesLocalDataSource,
    private val clock: AppClock,
) : MoviesRepository {
    private val refreshMutex = Mutex()

    override fun observeWeeklyTrendingMovies(): Flow<List<Movie>> =
        localDataSource.observeWeeklyTrendingMovies()

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
                movies = movies,
                refreshedAtEpochMillis = clock.nowEpochMillis(),
            )
        }
    }

    private suspend fun fetchWeeklyTrendingMovies(): List<Movie> {
        val genres = remoteDataSource.getGenres()
        val firstPage = remoteDataSource.getWeeklyTrendingMoviesPage(
            page = FIRST_PAGE,
            genres = genres,
        )
        val moviesById = LinkedHashMap<Long, Movie>(MAX_TRENDING_MOVIES)
        firstPage.movies.forEach { movie -> moviesById.putIfAbsent(movie.id, movie) }

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
                                genres = genres,
                            )
                        }
                    }
                    .awaitAll()
            }

            pages.forEach { page ->
                page.movies.forEach { movie -> moviesById.putIfAbsent(movie.id, movie) }
            }
            nextPage = batchLastPage + 1
        }

        return moviesById.values.take(MAX_TRENDING_MOVIES)
    }

    override suspend fun getGenres(): AppResult<List<Genre>> = executeSafely {
        remoteDataSource.getGenres()
    }

    override suspend fun getMovieDetails(movieId: Long): AppResult<MovieDetails> = executeSafely {
        remoteDataSource.getMovieDetails(movieId)
    }

    private suspend fun <T> executeSafely(block: suspend () -> T): AppResult<T> = try {
        AppResult.Success(block())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (_: IOException) {
        AppResult.Error(AppError.NoInternet)
    } catch (http: HttpException) {
        AppResult.Error(http.toAppError())
    } catch (unexpected: Exception) {
        AppResult.Error(AppError.Unknown(unexpected))
    }

    private fun HttpException.toAppError(): AppError = when (code()) {
        HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> AppError.Unauthorized
        HTTP_NOT_FOUND -> AppError.NotFound
        else -> AppError.Server(statusCode = code())
    }

    private fun Long?.isFresh(): Boolean =
        this != null && this >= clock.nowEpochMillis() - CACHE_MAX_AGE_MILLIS

    private companion object {
        const val FIRST_PAGE = 1
        const val MAX_TRENDING_MOVIES = 100
        const val PAGE_BATCH_SIZE = 4
        const val CACHE_MAX_AGE_MILLIS = 60 * 60 * 1_000L
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
    }
}
