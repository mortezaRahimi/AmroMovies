package com.mortex.amromovies.feature.trending.presentation.mapper

import com.mortex.amromovies.core.domain.result.AppError
import com.mortex.amromovies.feature.trending.presentation.TrendingUiError
import org.junit.Assert.assertEquals
import org.junit.Test

class TrendingErrorMapperTest {

    @Test
    fun `application errors map to their feature UI errors`() {
        val mappings = listOf(
            AppError.NoInternet to TrendingUiError.NO_INTERNET,
            AppError.Unauthorized to TrendingUiError.UNAUTHORIZED,
            AppError.NotFound to TrendingUiError.NOT_FOUND,
            AppError.Server(statusCode = 503) to TrendingUiError.SERVER,
            AppError.Unknown(IllegalStateException()) to TrendingUiError.UNKNOWN,
        )

        mappings.forEach { (appError, expectedUiError) ->
            assertEquals(expectedUiError, appError.toTrendingUiError())
        }
    }
}
