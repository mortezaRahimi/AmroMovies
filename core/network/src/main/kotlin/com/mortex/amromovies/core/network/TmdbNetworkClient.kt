package com.mortex.amromovies.core.network

import com.mortex.amromovies.core.data.source.MoviesRemoteDataSource
import com.mortex.amromovies.core.data.source.RemoteDataException
import com.mortex.amromovies.core.data.source.model.RemoteGenre
import com.mortex.amromovies.core.data.source.model.RemoteMovieDetails
import com.mortex.amromovies.core.data.source.model.RemoteTrendingMoviesPage
import com.mortex.amromovies.core.network.mapper.toRemote
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

class TmdbNetworkClient(
    accessToken: String,
    enableHttpLogging: Boolean,
    baseUrl: String = TMDB_BASE_URL,
) : MoviesRemoteDataSource {
    init {
        require(accessToken.isNotBlank()) { "TMDB access token must not be blank." }
    }

    internal val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    internal val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .callTimeout(CALL_TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val authenticatedRequest = chain.request()
                .newBuilder()
                .header(AUTHORIZATION_HEADER, "Bearer $accessToken")
                .header(ACCEPT_HEADER, JSON_MEDIA_TYPE)
                .build()
            chain.proceed(authenticatedRequest)
        }
        .apply {
            if (enableHttpLogging) {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        redactHeader(AUTHORIZATION_HEADER)
                        level = HttpLoggingInterceptor.Level.BODY
                    },
                )
            }
        }
        .build()

    internal val api: TmdbApi = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(JSON_MEDIA_TYPE.toMediaType()))
        .build()
        .create(TmdbApi::class.java)

    override suspend fun getWeeklyTrendingMoviesPage(
        page: Int,
    ): RemoteTrendingMoviesPage = executeRemoteCall {
        api.getWeeklyTrendingMovies(page = page).toRemote()
    }

    override suspend fun getGenres(): List<RemoteGenre> = executeRemoteCall {
        api.getMovieGenres().toRemote()
    }

    override suspend fun getMovieDetails(movieId: Long): RemoteMovieDetails = executeRemoteCall {
        api.getMovieDetails(movieId).toRemote()
    }

    private suspend fun <T> executeRemoteCall(block: suspend () -> T): T = try {
        block()
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (io: IOException) {
        throw RemoteDataException.NoInternet(io)
    } catch (http: HttpException) {
        throw when (http.code()) {
            HTTP_UNAUTHORIZED, HTTP_FORBIDDEN -> RemoteDataException.Unauthorized
            HTTP_NOT_FOUND -> RemoteDataException.NotFound
            else -> RemoteDataException.Server(http.code())
        }
    } catch (serialization: SerializationException) {
        throw RemoteDataException.InvalidResponse(serialization)
    }

    private companion object {
        const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
        const val JSON_MEDIA_TYPE = "application/json"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val ACCEPT_HEADER = "Accept"
        const val CONNECT_TIMEOUT_SECONDS = 10L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
        const val CALL_TIMEOUT_SECONDS = 30L
        const val HTTP_UNAUTHORIZED = 401
        const val HTTP_FORBIDDEN = 403
        const val HTTP_NOT_FOUND = 404
    }
}
