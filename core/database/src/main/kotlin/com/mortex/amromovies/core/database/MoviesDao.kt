package com.mortex.amromovies.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.mortex.amromovies.core.database.entity.CacheMetadataEntity
import com.mortex.amromovies.core.database.entity.GenreEntity
import com.mortex.amromovies.core.database.entity.MovieEntity
import com.mortex.amromovies.core.database.entity.MovieGenreCrossRef
import com.mortex.amromovies.core.database.entity.TrendingMovieEntity
import com.mortex.amromovies.core.database.entity.TrendingMovieWithGenres
import kotlinx.coroutines.flow.Flow

@Dao
internal abstract class MoviesDao {
    @Transaction
    @Query(
        """
        SELECT movies.*, trending_movies.rank AS trendingRank
        FROM trending_movies
        INNER JOIN movies ON movies.id = trending_movies.movieId
        ORDER BY trending_movies.rank ASC
        """,
    )
    abstract fun observeWeeklyTrendingMovies(): Flow<List<TrendingMovieWithGenres>>

    @Query("SELECT updatedAtEpochMillis FROM cache_metadata WHERE `key` = :key")
    abstract suspend fun getCacheUpdatedAt(key: String): Long?

    @Upsert
    protected abstract suspend fun upsertMovies(movies: List<MovieEntity>)

    @Upsert
    protected abstract suspend fun upsertGenres(genres: List<GenreEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertMovieGenreRefs(refs: List<MovieGenreCrossRef>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    protected abstract suspend fun insertTrendingMovies(movies: List<TrendingMovieEntity>)

    @Upsert
    protected abstract suspend fun upsertMetadata(metadata: CacheMetadataEntity)

    @Query("DELETE FROM movie_genres WHERE movieId IN (:movieIds)")
    protected abstract suspend fun deleteMovieGenreRefs(movieIds: List<Long>)

    @Query("DELETE FROM trending_movies")
    protected abstract suspend fun clearTrendingMovies()

    @Query("DELETE FROM movies WHERE id NOT IN (SELECT movieId FROM trending_movies)")
    protected abstract suspend fun deleteUnrankedMovies()

    @Query("DELETE FROM genres WHERE id NOT IN (SELECT genreId FROM movie_genres)")
    protected abstract suspend fun deleteUnusedGenres()

    @Transaction
    open suspend fun replaceWeeklyTrendingMovies(
        movies: List<MovieEntity>,
        genres: List<GenreEntity>,
        movieGenreRefs: List<MovieGenreCrossRef>,
        trendingMovies: List<TrendingMovieEntity>,
        metadata: CacheMetadataEntity,
    ) {
        upsertGenres(genres)
        upsertMovies(movies)
        if (movies.isNotEmpty()) {
            deleteMovieGenreRefs(movies.map(MovieEntity::id))
        }
        if (movieGenreRefs.isNotEmpty()) {
            insertMovieGenreRefs(movieGenreRefs)
        }
        clearTrendingMovies()
        if (trendingMovies.isNotEmpty()) {
            insertTrendingMovies(trendingMovies)
        }
        deleteUnrankedMovies()
        deleteUnusedGenres()
        upsertMetadata(metadata)
    }
}
