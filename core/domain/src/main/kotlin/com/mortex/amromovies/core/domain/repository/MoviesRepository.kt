package com.mortex.amromovies.core.domain.repository

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails
import com.mortex.amromovies.core.domain.result.AppResult
import kotlinx.coroutines.flow.Flow

interface MoviesRepository {
    fun observeWeeklyTrendingMovies(): Flow<List<Movie>>

    suspend fun refreshWeeklyTrendingMovies(force: Boolean = false): AppResult<Unit>

    suspend fun getGenres(): AppResult<List<Genre>>

    suspend fun getMovieDetails(movieId: Long): AppResult<MovieDetails>
}
