package com.mortex.amromovies.core.database

import android.content.Context
import androidx.room.Room
import com.mortex.amromovies.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow

interface MoviesLocalDataSource {
    fun observeWeeklyTrendingMovies(): Flow<List<Movie>>

    suspend fun replaceWeeklyTrendingMovies(
        movies: List<Movie>,
        refreshedAtEpochMillis: Long,
    )

    suspend fun getWeeklyTrendingUpdatedAt(): Long?
}

fun createMoviesLocalDataSource(context: Context): MoviesLocalDataSource {
    val database = Room.databaseBuilder(
        context.applicationContext,
        AmroMoviesDatabase::class.java,
        DATABASE_NAME,
    ).build()
    return RoomMoviesLocalDataSource(database)
}

private const val DATABASE_NAME = "amro-movies.db"
