package com.mortex.amromovies.core.network

import com.mortex.amromovies.core.network.model.GenreListResponseDto
import com.mortex.amromovies.core.network.model.MovieDetailsDto
import com.mortex.amromovies.core.network.model.TrendingMoviesResponseDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface TmdbApi {
    @GET("trending/movie/week")
    suspend fun getWeeklyTrendingMovies(
        @Query("page") page: Int,
        @Query("language") language: String = DEFAULT_LANGUAGE,
    ): TrendingMoviesResponseDto

    @GET("genre/movie/list")
    suspend fun getMovieGenres(
        @Query("language") language: String = DEFAULT_LANGUAGE,
    ): GenreListResponseDto

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Long,
        @Query("language") language: String = DEFAULT_LANGUAGE,
    ): MovieDetailsDto

    private companion object {
        const val DEFAULT_LANGUAGE = "en-US"
    }
}
