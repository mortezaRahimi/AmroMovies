package com.mortex.amromovies.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mortex.amromovies.core.database.entity.CacheMetadataEntity
import com.mortex.amromovies.core.database.entity.GenreEntity
import com.mortex.amromovies.core.database.entity.MovieEntity
import com.mortex.amromovies.core.database.entity.MovieGenreCrossRef
import com.mortex.amromovies.core.database.entity.TrendingMovieEntity

@Database(
    entities = [
        MovieEntity::class,
        GenreEntity::class,
        MovieGenreCrossRef::class,
        TrendingMovieEntity::class,
        CacheMetadataEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
internal abstract class AmroMoviesDatabase : RoomDatabase() {
    abstract fun moviesDao(): MoviesDao
}
