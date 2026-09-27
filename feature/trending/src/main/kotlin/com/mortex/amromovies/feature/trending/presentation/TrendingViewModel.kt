package com.mortex.amromovies.feature.trending.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.repository.MoviesRepository
import com.mortex.amromovies.core.domain.result.AppResult
import com.mortex.amromovies.feature.trending.presentation.mapper.toTrendingUiError
import com.mortex.amromovies.feature.trending.presentation.mapper.toUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieSort
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import com.mortex.amromovies.feature.trending.presentation.model.SortDirection
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TrendingViewModel(
    private val moviesRepository: MoviesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(TrendingUiState())
    val uiState: StateFlow<TrendingUiState> = _uiState.asStateFlow()

    private var canonicalMovies: List<MovieUiModel> = emptyList()
    private var refreshJob: Job? = null

    init {
        observeTrendingMovies()
        refresh(force = false, showInitialLoading = true)
    }

    fun onAction(action: TrendingAction) {
        when (action) {
            is TrendingAction.SelectGenre -> updateSelection(selectedGenreId = action.genreId)
            is TrendingAction.SelectSort -> updateSelection(sortOption = action.sort)
            is TrendingAction.SelectSortDirection -> updateSelection(sortDirection = action.direction)
            TrendingAction.ToggleSortDirection -> updateSelection(
                sortDirection = _uiState.value.sortDirection.toggled(),
            )
            TrendingAction.Refresh -> refresh(force = true, showInitialLoading = false)
            TrendingAction.Retry -> refresh(
                force = true,
                showInitialLoading = canonicalMovies.isEmpty(),
            )
        }
    }

    private fun observeTrendingMovies() {
        viewModelScope.launch {
            moviesRepository.observeWeeklyTrendingMovies().collect { movies ->
                canonicalMovies = movies.take(MAX_TRENDING_MOVIES).map(Movie::toUiModel)
                updateVisibleContent()
            }
        }
    }

    private fun refresh(force: Boolean, showInitialLoading: Boolean) {
        if (refreshJob?.isActive == true) return

        _uiState.update { state ->
            state.copy(
                isLoading = showInitialLoading,
                isRefreshing = !showInitialLoading,
                error = null,
            )
        }
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            when (val result = moviesRepository.refreshWeeklyTrendingMovies(force)) {
                is AppResult.Success -> _uiState.update { state ->
                    state.copy(isLoading = false, isRefreshing = false, error = null)
                }
                is AppResult.Error -> _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isRefreshing = false,
                        error = result.error.toTrendingUiError(),
                    )
                }
            }
        }
        refreshJob = job
        job.start()
    }

    private fun updateSelection(
        selectedGenreId: Int? = _uiState.value.selectedGenreId,
        sortOption: MovieSort = _uiState.value.sortOption,
        sortDirection: SortDirection = _uiState.value.sortDirection,
    ) {
        _uiState.update { state ->
            state.copy(
                movies = filterAndSortMovies(
                    movies = canonicalMovies,
                    selectedGenreId = selectedGenreId,
                    sort = sortOption,
                    direction = sortDirection,
                ),
                selectedGenreId = selectedGenreId,
                sortOption = sortOption,
                sortDirection = sortDirection,
            )
        }
    }

    private fun updateVisibleContent() {
        _uiState.update { state ->
            state.copy(
                movies = filterAndSortMovies(
                    movies = canonicalMovies,
                    selectedGenreId = state.selectedGenreId,
                    sort = state.sortOption,
                    direction = state.sortDirection,
                ),
                genres = availableGenres(canonicalMovies),
                hasUnfilteredMovies = canonicalMovies.isNotEmpty(),
            )
        }
    }

    private fun SortDirection.toggled(): SortDirection = when (this) {
        SortDirection.ASCENDING -> SortDirection.DESCENDING
        SortDirection.DESCENDING -> SortDirection.ASCENDING
    }

    private companion object {
        const val MAX_TRENDING_MOVIES = 100
    }
}
