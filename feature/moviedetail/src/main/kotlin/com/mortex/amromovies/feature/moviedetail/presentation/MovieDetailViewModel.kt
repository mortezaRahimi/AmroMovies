package com.mortex.amromovies.feature.moviedetail.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mortex.amromovies.core.domain.repository.MoviesRepository
import com.mortex.amromovies.core.domain.result.AppResult
import com.mortex.amromovies.feature.moviedetail.presentation.mapper.toMovieDetailUiError
import com.mortex.amromovies.feature.moviedetail.presentation.mapper.toUiModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MovieDetailViewModel(
    private val movieId: Long,
    private val moviesRepository: MoviesRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MovieDetailUiState())
    val uiState: StateFlow<MovieDetailUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    init {
        loadDetails()
    }

    fun onAction(action: MovieDetailAction) {
        when (action) {
            MovieDetailAction.Retry -> loadDetails()
        }
    }

    private fun loadDetails() {
        if (loadJob?.isActive == true) return

        _uiState.update { state -> state.copy(isLoading = true, error = null) }
        val job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            when (val result = moviesRepository.getMovieDetails(movieId)) {
                is AppResult.Success -> _uiState.value = MovieDetailUiState(
                    movie = result.data.toUiModel(),
                    isLoading = false,
                )
                is AppResult.Error -> _uiState.value = MovieDetailUiState(
                    isLoading = false,
                    error = result.error.toMovieDetailUiError(),
                )
            }
        }
        loadJob = job
        job.start()
    }
}
