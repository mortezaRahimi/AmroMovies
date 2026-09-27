package com.mortex.amromovies.core.network.mapper

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails
import com.mortex.amromovies.core.network.model.GenreDto
import com.mortex.amromovies.core.network.model.GenreListResponseDto
import com.mortex.amromovies.core.network.model.MovieDetailsDto
import com.mortex.amromovies.core.network.model.TrendingMovieDto
import com.mortex.amromovies.core.network.model.TrendingMoviesResponseDto
import java.time.LocalDate
import java.time.format.DateTimeParseException

internal fun GenreDto.toDomain(): Genre = Genre(
    id = id,
    name = name,
)

internal fun GenreListResponseDto.toDomain(): List<Genre> = genres.map(GenreDto::toDomain)

internal fun TrendingMoviesResponseDto.toDomain(genres: List<Genre>): List<Movie> {
    val genresById = genres.associateBy(Genre::id)
    return results.map { movie -> movie.toDomain(genresById) }
}

private fun TrendingMovieDto.toDomain(genresById: Map<Int, Genre>): Movie = Movie(
    id = id,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    genres = genreIds.mapNotNull(genresById::get),
    releaseDate = releaseDate.toLocalDateOrNull(),
    originalLanguage = originalLanguage,
    popularity = popularity,
    rating = voteAverage,
    voteCount = voteCount,
    isAdult = adult,
)

internal fun MovieDetailsDto.toDomain(): MovieDetails = MovieDetails(
    id = id,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    tagline = tagline,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate.toLocalDateOrNull(),
    runtimeMinutes = runtime,
    status = status,
    genres = genres.map(GenreDto::toDomain),
    originalLanguage = originalLanguage,
    popularity = popularity,
    rating = voteAverage,
    voteCount = voteCount,
    isAdult = adult,
    budget = budget,
    revenue = revenue,
    homepageUrl = homepage,
    imdbId = imdbId,
)

private fun String.toLocalDateOrNull(): LocalDate? {
    if (isBlank()) return null
    return try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        null
    }
}
