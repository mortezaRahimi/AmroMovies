package com.mortex.amromovies.core.database

import com.mortex.amromovies.core.database.entity.CacheMetadataEntity
import com.mortex.amromovies.core.database.entity.GenreEntity
import com.mortex.amromovies.core.database.entity.MovieEntity
import com.mortex.amromovies.core.database.entity.MovieGenreCrossRef
import com.mortex.amromovies.core.database.entity.TrendingMovieEntity
import com.mortex.amromovies.core.database.entity.TrendingMovieWithGenres
import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

internal class RoomMoviesLocalDataSource(
    private val database: AmroMoviesDatabase,
) : MoviesLocalDataSource {
    private val dao = database.moviesDao()

    override fun observeWeeklyTrendingMovies(): Flow<List<Movie>> =
        dao.observeWeeklyTrendingMovies().map { movies ->
            movies.map { movie -> movie.toDomain() }
        }

    override suspend fun replaceWeeklyTrendingMovies(
        movies: List<Movie>,
        refreshedAtEpochMillis: Long,
    ) {
        val genres = movies
            .flatMap(Movie::genres)
            .distinctBy(Genre::id)
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

    private fun Movie.toEntity() = MovieEntity(
        id = id,
        title = title,
        originalTitle = originalTitle,
        overview = overview,
        posterPath = posterPath,
        backdropPath = backdropPath,
        releaseDateEpochDay = releaseDate?.toEpochDay(),
        originalLanguage = originalLanguage,
        popularity = popularity,
        rating = rating,
        voteCount = voteCount,
        isAdult = isAdult,
    )

    private fun Genre.toEntity() = GenreEntity(id = id, name = name)

    private fun TrendingMovieWithGenres.toDomain() = Movie(
        id = movie.id,
        title = movie.title,
        originalTitle = movie.originalTitle,
        overview = movie.overview,
        posterPath = movie.posterPath,
        backdropPath = movie.backdropPath,
        genres = genres.map { genre -> Genre(id = genre.id, name = genre.name) },
        releaseDate = movie.releaseDateEpochDay?.let(LocalDate::ofEpochDay),
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
