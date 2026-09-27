package com.mortex.amromovies.core.data.source

sealed class RemoteDataException(
    cause: Throwable? = null,
) : Exception(cause) {
    class NoInternet(cause: Throwable) : RemoteDataException(cause)

    data object Unauthorized : RemoteDataException()

    data object NotFound : RemoteDataException()

    class Server(val statusCode: Int) : RemoteDataException()

    class InvalidResponse(cause: Throwable) : RemoteDataException(cause)
}
