package com.mortex.amromovies.core.domain.result

sealed interface AppError {
    data object NoInternet : AppError

    data object Unauthorized : AppError

    data object NotFound : AppError

    data class Server(val statusCode: Int? = null) : AppError

    data class Unknown(val cause: Throwable? = null) : AppError
}
