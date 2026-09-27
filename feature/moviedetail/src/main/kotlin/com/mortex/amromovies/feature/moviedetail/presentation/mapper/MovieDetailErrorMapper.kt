package com.mortex.amromovies.feature.moviedetail.presentation.mapper

import com.mortex.amromovies.core.domain.result.AppError
import com.mortex.amromovies.feature.moviedetail.presentation.MovieDetailUiError

internal fun AppError.toMovieDetailUiError(): MovieDetailUiError = when (this) {
    AppError.NoInternet -> MovieDetailUiError.NO_INTERNET
    AppError.Unauthorized -> MovieDetailUiError.UNAUTHORIZED
    AppError.NotFound -> MovieDetailUiError.NOT_FOUND
    is AppError.Server -> MovieDetailUiError.SERVER
    is AppError.Unknown -> MovieDetailUiError.UNKNOWN
}
