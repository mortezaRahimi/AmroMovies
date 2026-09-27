package com.mortex.amromovies.core.network

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails
import com.mortex.amromovies.core.network.mapper.toDomain
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

class TmdbNetworkClient(
    accessToken: String,
    enableHttpLogging: Boolean,
    baseUrl: String = TMDB_BASE_URL,
) : TmdbRemoteDataSource {
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
                        level = HttpLoggingInterceptor.Level.BASIC
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
        genres: List<Genre>,
    ): TrendingMoviesPage = api.getWeeklyTrendingMovies(page = page).let { response ->
        TrendingMoviesPage(
            page = response.page,
            totalPages = response.totalPages,
            movies = response.toDomain(genres),
        )
    }

    override suspend fun getGenres(): List<Genre> = api.getMovieGenres().toDomain()

    override suspend fun getMovieDetails(movieId: Long): MovieDetails =
        api.getMovieDetails(movieId).toDomain()

    private companion object {
        const val TMDB_BASE_URL = "https://api.themoviedb.org/3/"
        const val JSON_MEDIA_TYPE = "application/json"
        const val AUTHORIZATION_HEADER = "Authorization"
        const val ACCEPT_HEADER = "Accept"
        const val CONNECT_TIMEOUT_SECONDS = 10L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
        const val CALL_TIMEOUT_SECONDS = 30L
    }
}
