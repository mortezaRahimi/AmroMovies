package com.mortex.amromovies.feature.trending.presentation

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails
import com.mortex.amromovies.core.domain.repository.MoviesRepository
import com.mortex.amromovies.core.domain.result.AppError
import com.mortex.amromovies.core.domain.result.AppResult
import com.mortex.amromovies.feature.trending.presentation.model.MovieSort
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import com.mortex.amromovies.feature.trending.presentation.model.SortDirection
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TrendingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `initial state represents initial loading`() = runTest(mainDispatcherRule.dispatcher) {
        val pendingRefresh = CompletableDeferred<AppResult<Unit>>()
        val repository = FakeMoviesRepository { pendingRefresh.await() }

        val viewModel = TrendingViewModel(repository)

        assertTrue(viewModel.uiState.value.isLoading)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(viewModel.uiState.value.movies.isEmpty())
    }

    @Test
    fun `cached movies and successful initial refresh produce content`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeMoviesRepository(
            initialMovies = movies,
            refreshHandler = { AppResult.Success(Unit) },
        )

        val viewModel = TrendingViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isRefreshing)
        assertNull(state.error)
        assertEquals(listOf(3L, 1L, 2L), state.movies.map(MovieUiModel::id))
        assertEquals(listOf(1, 2), state.genres.map { genre -> genre.id })
        assertEquals(listOf(false), repository.refreshForces)
    }

    @Test
    fun `application error maps into UI error`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = FakeMoviesRepository {
            AppResult.Error(AppError.NoInternet)
        }

        val viewModel = TrendingViewModel(repository)
        advanceUntilIdle()

        assertEquals(TrendingUiError.NO_INTERNET, viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `retry clears error forces refresh and publishes success`() = runTest(mainDispatcherRule.dispatcher) {
        var result: AppResult<Unit> = AppResult.Error(AppError.Server(500))
        val repository = FakeMoviesRepository { result }
        val viewModel = TrendingViewModel(repository)
        advanceUntilIdle()
        assertEquals(TrendingUiError.SERVER, viewModel.uiState.value.error)

        result = AppResult.Success(Unit)
        repository.movies.value = movies
        viewModel.onAction(TrendingAction.Retry)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(3, viewModel.uiState.value.movies.size)
        assertEquals(listOf(false, true), repository.refreshForces)
    }

    @Test
    fun `manual refresh keeps content and exposes refreshing separately`() = runTest(mainDispatcherRule.dispatcher) {
        val refreshGate = CompletableDeferred<AppResult<Unit>>()
        var initialRefresh = true
        val repository = FakeMoviesRepository(initialMovies = movies) {
            if (initialRefresh) {
                initialRefresh = false
                AppResult.Success(Unit)
            } else {
                refreshGate.await()
            }
        }
        val viewModel = TrendingViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(TrendingAction.Refresh)
        runCurrent()

        assertFalse(viewModel.uiState.value.isLoading)
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertEquals(3, viewModel.uiState.value.movies.size)

        refreshGate.complete(AppResult.Success(Unit))
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertEquals(listOf(false, true), repository.refreshForces)
    }

    @Test
    fun `genre selection filters locally without refreshing`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = successfulRepository()
        val viewModel = TrendingViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(TrendingAction.SelectGenre(ACTION.id))

        assertEquals(listOf(3L, 1L), viewModel.uiState.value.movies.map(MovieUiModel::id))
        assertEquals(ACTION.id, viewModel.uiState.value.selectedGenreId)
        assertEquals(1, repository.refreshForces.size)
    }

    @Test
    fun `sort selection updates visible movies locally`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = successfulRepository()
        val viewModel = TrendingViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(TrendingAction.SelectSort(MovieSort.TITLE))
        viewModel.onAction(TrendingAction.SelectSortDirection(SortDirection.ASCENDING))

        assertEquals(listOf(2L, 1L, 3L), viewModel.uiState.value.movies.map(MovieUiModel::id))
        assertEquals(MovieSort.TITLE, viewModel.uiState.value.sortOption)
        assertEquals(SortDirection.ASCENDING, viewModel.uiState.value.sortDirection)
        assertEquals(1, repository.refreshForces.size)
    }

    @Test
    fun `direction toggle reverses the selected ordering`() = runTest(mainDispatcherRule.dispatcher) {
        val viewModel = TrendingViewModel(successfulRepository())
        advanceUntilIdle()
        assertEquals(SortDirection.DESCENDING, viewModel.uiState.value.sortDirection)

        viewModel.onAction(TrendingAction.ToggleSortDirection)

        assertEquals(SortDirection.ASCENDING, viewModel.uiState.value.sortDirection)
        assertEquals(listOf(2L, 1L, 3L), viewModel.uiState.value.movies.map(MovieUiModel::id))
    }

    @Test
    fun `genre with no matching movie produces an empty content list`() = runTest(mainDispatcherRule.dispatcher) {
        val repository = successfulRepository()
        val viewModel = TrendingViewModel(repository)
        advanceUntilIdle()

        viewModel.onAction(TrendingAction.SelectGenre(999))

        assertTrue(viewModel.uiState.value.movies.isEmpty())
        assertEquals(1, repository.refreshForces.size)
    }

    private fun successfulRepository() = FakeMoviesRepository(
        initialMovies = movies,
        refreshHandler = { AppResult.Success(Unit) },
    )

    private val movies = listOf(
        movie(1, "beta", 20.0, listOf(ACTION)),
        movie(2, "Alpha", 10.0, listOf(COMEDY)),
        movie(3, "charlie", 30.0, listOf(ACTION, COMEDY)),
    )

    private fun movie(
        id: Long,
        title: String,
        popularity: Double,
        genres: List<Genre>,
    ) = Movie(
        id = id,
        title = title,
        originalTitle = title,
        overview = "Overview",
        posterPath = null,
        backdropPath = null,
        genres = genres,
        releaseDate = LocalDate.of(2025, 1, id.toInt()),
        originalLanguage = "en",
        popularity = popularity,
        rating = 8.0,
        voteCount = 100,
        isAdult = false,
    )

    private companion object {
        val ACTION = Genre(1, "Action")
        val COMEDY = Genre(2, "Comedy")
    }
}

private class FakeMoviesRepository(
    initialMovies: List<Movie> = emptyList(),
    private val refreshHandler: suspend (force: Boolean) -> AppResult<Unit>,
) : MoviesRepository {
    val movies = MutableStateFlow(initialMovies)
    val refreshForces = mutableListOf<Boolean>()

    override fun observeWeeklyTrendingMovies(): Flow<List<Movie>> = movies

    override suspend fun refreshWeeklyTrendingMovies(force: Boolean): AppResult<Unit> {
        refreshForces += force
        return refreshHandler(force)
    }

    override suspend fun getGenres(): AppResult<List<Genre>> = error("Not used")

    override suspend fun getMovieDetails(movieId: Long): AppResult<MovieDetails> = error("Not used")
}
