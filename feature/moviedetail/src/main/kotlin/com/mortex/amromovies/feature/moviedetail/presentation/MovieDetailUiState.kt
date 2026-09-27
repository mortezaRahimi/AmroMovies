package com.mortex.amromovies.feature.moviedetail.presentation

import androidx.compose.runtime.Immutable
import com.mortex.amromovies.feature.moviedetail.presentation.model.MovieDetailUiModel

@Immutable
data class MovieDetailUiState(
    val movie: MovieDetailUiModel? = null,
    val isLoading: Boolean = true,
    val error: MovieDetailUiError? = null,
)

enum class MovieDetailUiError {
    NO_INTERNET,
    UNAUTHORIZED,
    NOT_FOUND,
    SERVER,
    UNKNOWN,
}
