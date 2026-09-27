package com.mortex.amromovies.core.network

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails

interface TmdbRemoteDataSource {
    suspend fun getWeeklyTrendingMoviesPage(
        page: Int,
        genres: List<Genre>,
    ): TrendingMoviesPage

    suspend fun getGenres(): List<Genre>

    suspend fun getMovieDetails(movieId: Long): MovieDetails
}

data class TrendingMoviesPage(
    val page: Int,
    val totalPages: Int,
    val movies: List<Movie>,
)
