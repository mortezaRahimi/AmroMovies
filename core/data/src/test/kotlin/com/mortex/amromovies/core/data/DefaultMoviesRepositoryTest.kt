package com.mortex.amromovies.core.data

import com.mortex.amromovies.core.common.time.AppClock
import com.mortex.amromovies.core.data.source.MoviesLocalDataSource
import com.mortex.amromovies.core.data.source.MoviesRemoteDataSource
import com.mortex.amromovies.core.data.source.RemoteDataException
import com.mortex.amromovies.core.data.source.model.CachedGenre
import com.mortex.amromovies.core.data.source.model.CachedMovie
import com.mortex.amromovies.core.data.source.model.RemoteGenre
import com.mortex.amromovies.core.data.source.model.RemoteMovie
import com.mortex.amromovies.core.data.source.model.RemoteMovieDetails
import com.mortex.amromovies.core.data.source.model.RemoteTrendingMoviesPage
import com.mortex.amromovies.core.domain.result.AppError
import com.mortex.amromovies.core.domain.result.AppResult
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class DefaultMoviesRepositoryTest {
    @Test
    fun `refresh aggregates pages and writes exactly 100 ordered unique movies`() = runTest {
        val remote = FakeRemoteDataSource(
            pages = mapOf(
                1 to movies(1..20),
                2 to movies(20..39),
                3 to movies(40..59),
                4 to movies(60..79),
                5 to movies(80..99),
                6 to movies(100..119),
            ),
            totalPages = 6,
        )
        val local = FakeLocalDataSource()

        val result = repository(remote, local).refreshWeeklyTrendingMovies()

        assertEquals(AppResult.Success(Unit), result)
        assertEquals((1L..100L).toList(), local.movies.value.map(CachedMovie::id))
        assertEquals(listOf(1, 2, 3, 4, 5, 6), remote.requestedPages.sorted())
    }

    @Test
    fun `refresh stores fewer than 100 when TMDB has fewer results`() = runTest {
        val remote = FakeRemoteDataSource(
            pages = mapOf(1 to movies(1..20), 2 to movies(21..35)),
            totalPages = 2,
        )
        val local = FakeLocalDataSource()

        repository(remote, local).refreshWeeklyTrendingMovies()

        assertEquals((1L..35L).toList(), local.movies.value.map(CachedMovie::id))
    }

    @Test
    fun `concurrent page completion does not change trending order`() = runTest {
        val remote = FakeRemoteDataSource(
            pages = (1..5).associateWith { page ->
                val firstId = (page - 1) * 20 + 1
                movies(firstId..firstId + 19)
            },
            totalPages = 5,
            delaysByPage = mapOf(2 to 40L, 3 to 30L, 4 to 20L, 5 to 10L),
        )
        val local = FakeLocalDataSource()

        repository(remote, local).refreshWeeklyTrendingMovies()

        assertEquals((1L..100L).toList(), local.movies.value.map(CachedMovie::id))
    }

    @Test
    fun `fresh cache skips network unless refresh is forced`() = runTest {
        val local = FakeLocalDataSource(updatedAt = NOW - 1_000L)
        val remote = FakeRemoteDataSource(pages = mapOf(1 to movies(1..2)))
        val repository = repository(remote, local)

        repository.refreshWeeklyTrendingMovies()
        assertEquals(0, remote.genreRequestCount)

        repository.refreshWeeklyTrendingMovies(force = true)
        assertEquals(1, remote.genreRequestCount)
    }

    @Test
    fun `concurrent refresh requests share one network refresh`() = runTest {
        val local = FakeLocalDataSource()
        val remote = FakeRemoteDataSource(
            pages = mapOf(1 to movies(1..20)),
            firstPageDelayMillis = 100L,
        )
        val repository = repository(remote, local)

        awaitAll(
            async { repository.refreshWeeklyTrendingMovies() },
            async { repository.refreshWeeklyTrendingMovies() },
        )

        assertEquals(1, remote.genreRequestCount)
        assertEquals(1, local.replaceCount)
    }

    @Test
    fun `cached flow remains available when refresh has no internet`() = runTest {
        val cached = cachedMovies(1..3)
        val local = FakeLocalDataSource(initialMovies = cached)
        val repository = repository(
            remote = FakeRemoteDataSource(
                failure = RemoteDataException.NoInternet(IOException("offline")),
            ),
            local = local,
        )

        val result = repository.refreshWeeklyTrendingMovies(force = true)

        assertEquals(AppResult.Error(AppError.NoInternet), result)
        assertEquals(cached, local.movies.value)
    }

    @Test
    fun `maps HTTP failures and preserves cached content`() = runTest {
        val cached = cachedMovies(1..2)
        val local = FakeLocalDataSource(initialMovies = cached)
        val repository = repository(
            remote = FakeRemoteDataSource(failure = RemoteDataException.Server(503)),
            local = local,
        )

        val result = repository.refreshWeeklyTrendingMovies(force = true)

        assertEquals(AppResult.Error(AppError.Server(503)), result)
        assertEquals(cached, local.movies.value)
    }

    @Test
    fun `rethrows cancellation`() = runTest {
        val repository = repository(
            remote = FakeRemoteDataSource(failure = CancellationException("cancelled")),
            local = FakeLocalDataSource(),
        )

        try {
            repository.refreshWeeklyTrendingMovies(force = true)
            fail("CancellationException should propagate")
        } catch (expected: CancellationException) {
            assertEquals("cancelled", expected.message)
        }
    }

    private fun repository(
        remote: FakeRemoteDataSource,
        local: FakeLocalDataSource,
    ) = DefaultMoviesRepository(
        remoteDataSource = remote,
        localDataSource = local,
        clock = AppClock { NOW },
    )

    private fun movies(range: IntRange): List<RemoteMovie> = range.map { id -> movie(id.toLong()) }

    private fun movie(id: Long) = RemoteMovie(
        id = id,
        title = "Movie $id",
        originalTitle = "Movie $id",
        overview = "Overview $id",
        posterPath = null,
        backdropPath = null,
        genreIds = listOf(18),
        releaseDate = "",
        originalLanguage = "en",
        popularity = 1000.0 - id,
        rating = 8.0,
        voteCount = 100,
        isAdult = false,
    )

    private fun cachedMovies(range: IntRange): List<CachedMovie> = range.map { id ->
        CachedMovie(
            id = id.toLong(),
            title = "Movie $id",
            originalTitle = "Movie $id",
            overview = "Overview $id",
            posterPath = null,
            backdropPath = null,
            genres = listOf(CachedGenre(18, "Drama")),
            releaseDateEpochDay = null,
            originalLanguage = "en",
            popularity = 1000.0 - id,
            rating = 8.0,
            voteCount = 100,
            isAdult = false,
        )
    }

    private class FakeLocalDataSource(
        initialMovies: List<CachedMovie> = emptyList(),
        var updatedAt: Long? = null,
    ) : MoviesLocalDataSource {
        val movies = MutableStateFlow(initialMovies)
        var replaceCount = 0

        override fun observeWeeklyTrendingMovies(): Flow<List<CachedMovie>> = movies

        override suspend fun replaceWeeklyTrendingMovies(
            movies: List<CachedMovie>,
            refreshedAtEpochMillis: Long,
        ) {
            replaceCount++
            this.movies.value = movies
            updatedAt = refreshedAtEpochMillis
        }

        override suspend fun getWeeklyTrendingUpdatedAt(): Long? = updatedAt
    }

    private class FakeRemoteDataSource(
        private val pages: Map<Int, List<RemoteMovie>> = emptyMap(),
        private val totalPages: Int = 1,
        private val delaysByPage: Map<Int, Long> = emptyMap(),
        private val firstPageDelayMillis: Long = 0L,
        private val failure: Throwable? = null,
    ) : MoviesRemoteDataSource {
        val requestedPages = mutableListOf<Int>()
        var genreRequestCount = 0

        override suspend fun getWeeklyTrendingMoviesPage(
            page: Int,
        ): RemoteTrendingMoviesPage {
            failure?.let { throw it }
            requestedPages += page
            delay(if (page == 1) firstPageDelayMillis else delaysByPage[page] ?: 0L)
            return RemoteTrendingMoviesPage(page, totalPages, pages[page].orEmpty())
        }

        override suspend fun getGenres(): List<RemoteGenre> {
            failure?.let { throw it }
            genreRequestCount++
            return listOf(RemoteGenre(id = 18, name = "Drama"))
        }

        override suspend fun getMovieDetails(movieId: Long): RemoteMovieDetails =
            error("Not needed by these tests")
    }

    private companion object {
        const val NOW = 1_800_000_000_000L
    }
}
