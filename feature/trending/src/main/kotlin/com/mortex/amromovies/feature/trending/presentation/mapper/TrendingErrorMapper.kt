package com.mortex.amromovies.feature.trending.presentation.mapper

import com.mortex.amromovies.core.domain.result.AppError
import com.mortex.amromovies.feature.trending.presentation.TrendingUiError

internal fun AppError.toTrendingUiError(): TrendingUiError = when (this) {
    AppError.NoInternet -> TrendingUiError.NO_INTERNET
    AppError.Unauthorized -> TrendingUiError.UNAUTHORIZED
    AppError.NotFound -> TrendingUiError.NOT_FOUND
    is AppError.Server -> TrendingUiError.SERVER
    is AppError.Unknown -> TrendingUiError.UNKNOWN
}
