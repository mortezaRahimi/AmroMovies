package com.mortex.amromovies.core.data.source

import com.mortex.amromovies.core.data.source.model.RemoteGenre
import com.mortex.amromovies.core.data.source.model.RemoteMovieDetails
import com.mortex.amromovies.core.data.source.model.RemoteTrendingMoviesPage

interface MoviesRemoteDataSource {
    suspend fun getWeeklyTrendingMoviesPage(page: Int): RemoteTrendingMoviesPage

    suspend fun getGenres(): List<RemoteGenre>

    suspend fun getMovieDetails(movieId: Long): RemoteMovieDetails
}
