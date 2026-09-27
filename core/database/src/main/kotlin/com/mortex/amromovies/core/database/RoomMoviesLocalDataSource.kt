package com.mortex.amromovies.core.database

import com.mortex.amromovies.core.database.entity.CacheMetadataEntity
import com.mortex.amromovies.core.database.entity.GenreEntity
import com.mortex.amromovies.core.database.entity.MovieEntity
import com.mortex.amromovies.core.database.entity.MovieGenreCrossRef
import com.mortex.amromovies.core.database.entity.TrendingMovieEntity
import com.mortex.amromovies.core.database.entity.TrendingMovieWithGenres
import com.mortex.amromovies.core.data.source.MoviesLocalDataSource
import com.mortex.amromovies.core.data.source.model.CachedGenre
import com.mortex.amromovies.core.data.source.model.CachedMovie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class RoomMoviesLocalDataSource(
    private val database: AmroMoviesDatabase,
) : MoviesLocalDataSource {
    private val dao = database.moviesDao()

    override fun observeWeeklyTrendingMovies(): Flow<List<CachedMovie>> =
        dao.observeWeeklyTrendingMovies().map { movies ->
            movies.map { movie -> movie.toCached() }
        }

    override suspend fun replaceWeeklyTrendingMovies(
        movies: List<CachedMovie>,
        refreshedAtEpochMillis: Long,
    ) {
        val genres = movies
            .flatMap(CachedMovie::genres)
            .distinctBy(CachedGenre::id)
            .map { genre -> genre.toEntity() }
        val movieGenreRefs = movies.flatMap { movie ->
            movie.genres.map { genre ->
                MovieGenreCrossRef(movieId = movie.id, genreId = genre.id)
            }
        }
        dao.replaceWeeklyTrendingMovies(
            movies = movies.map { movie -> movie.toEntity() },
            genres = genres,
            movieGenreRefs = movieGenreRefs,
            trendingMovies = movies.mapIndexed { index, movie ->
                TrendingMovieEntity(movieId = movie.id, rank = index + 1)
            },
            metadata = CacheMetadataEntity(
                key = WEEKLY_TRENDING_CACHE_KEY,
                updatedAtEpochMillis = refreshedAtEpochMillis,
            ),
        )
    }

    override suspend fun getWeeklyTrendingUpdatedAt(): Long? =
        dao.getCacheUpdatedAt(WEEKLY_TRENDING_CACHE_KEY)

    private fun CachedMovie.toEntity() = MovieEntity(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDateEpochDay = releaseDateEpochDay,
        originalLanguage = originalLanguage,
        popularity = popularity,
        rating = rating,
        voteCount = voteCount,
        isAdult = isAdult,
    )

    private fun CachedGenre.toEntity() = GenreEntity(id = id, name = name)

    private fun TrendingMovieWithGenres.toCached() = CachedMovie(
        id = movie.id,
        title = movie.title,
        originalTitle = movie.originalTitle,
        overview = movie.overview,
        posterPath = movie.posterPath,
        backdropPath = movie.backdropPath,
        genres = genres.map { genre -> CachedGenre(id = genre.id, name = genre.name) },
        releaseDateEpochDay = movie.releaseDateEpochDay,
        originalLanguage = movie.originalLanguage,
        popularity = movie.popularity,
        rating = movie.rating,
        voteCount = movie.voteCount,
        isAdult = movie.isAdult,
    )

    private companion object {
        const val WEEKLY_TRENDING_CACHE_KEY = "weekly_trending"
    }
}
