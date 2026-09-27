package com.mortex.amromovies.feature.moviedetail.presentation

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails
import com.mortex.amromovies.core.domain.repository.MoviesRepository
import com.mortex.amromovies.core.domain.result.AppError
import com.mortex.amromovies.core.domain.result.AppResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MovieDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state is loading and requests the navigation movie ID`() =
        runTest(mainDispatcherRule.dispatcher) {
            val pending = CompletableDeferred<AppResult<MovieDetails>>()
            val repository = FakeMoviesRepository { pending.await() }

            val viewModel = MovieDetailViewModel(movieId = 42, moviesRepository = repository)

            assertTrue(viewModel.uiState.value.isLoading)
            advanceUntilIdle()
            assertEquals(listOf(42L), repository.requestedMovieIds)
        }

    @Test
    fun `successful result publishes mapped movie details`() =
        runTest(mainDispatcherRule.dispatcher) {
            val repository = FakeMoviesRepository { AppResult.Success(testMovieDetails()) }

            val viewModel = MovieDetailViewModel(42, repository)
            advanceUntilIdle()

            assertFalse(viewModel.uiState.value.isLoading)
            assertNull(viewModel.uiState.value.error)
            assertEquals("Example Movie", viewModel.uiState.value.movie?.title)
        }

    @Test
    fun `typed repository error maps to UI error`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeMoviesRepository { AppResult.Error(AppError.NotFound) }

        val viewModel = MovieDetailViewModel(42, repository)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(MovieDetailUiError.NOT_FOUND, viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.movie)
    }

    @Test
    fun `retry clears error and loads the same movie ID`() = runTest(mainDispatcherRule.dispatcher) {
        var result: AppResult<MovieDetails> = AppResult.Error(AppError.NoInternet)
        val repository = FakeMoviesRepository { result }
        val viewModel = MovieDetailViewModel(42, repository)
        advanceUntilIdle()

        result = AppResult.Success(testMovieDetails())
        viewModel.onAction(MovieDetailAction.Retry)
        advanceUntilIdle()

        assertEquals(listOf(42L, 42L), repository.requestedMovieIds)
        assertNull(viewModel.uiState.value.error)
        assertEquals(42L, viewModel.uiState.value.movie?.id)
    }
}

private class FakeMoviesRepository(
    private val detailsHandler: suspend (Long) -> AppResult<MovieDetails>,
) : MoviesRepository {
    val requestedMovieIds = mutableListOf<Long>()

    override suspend fun getMovieDetails(movieId: Long): AppResult<MovieDetails> {
        requestedMovieIds += movieId
        return detailsHandler(movieId)
    }

    override fun observeWeeklyTrendingMovies(): Flow<List<Movie>> = emptyFlow()

    override suspend fun refreshWeeklyTrendingMovies(force: Boolean): AppResult<Unit> =
        error("Not used")

    override suspend fun getGenres(): AppResult<List<Genre>> = error("Not used")
}
