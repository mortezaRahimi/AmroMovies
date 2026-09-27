package com.mortex.amromovies.core.data.source

import com.mortex.amromovies.core.data.source.model.CachedMovie
import kotlinx.coroutines.flow.Flow

interface MoviesLocalDataSource {
    fun observeWeeklyTrendingMovies(): Flow<List<CachedMovie>>

    suspend fun replaceWeeklyTrendingMovies(
        movies: List<CachedMovie>,
        refreshedAtEpochMillis: Long,
    )

    suspend fun getWeeklyTrendingUpdatedAt(): Long?
}
