package com.mortex.amromovies.core.data.mapper

import com.mortex.amromovies.core.data.source.model.CachedGenre
import com.mortex.amromovies.core.data.source.model.CachedMovie
import com.mortex.amromovies.core.data.source.model.RemoteGenre
import com.mortex.amromovies.core.data.source.model.RemoteMovie
import com.mortex.amromovies.core.data.source.model.RemoteMovieDetails
import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.core.domain.model.MovieDetails
import java.time.LocalDate
import java.time.format.DateTimeParseException

internal fun RemoteGenre.toDomain(): Genre = Genre(id = id, name = name)

internal fun RemoteMovie.toDomain(genresById: Map<Int, RemoteGenre>): Movie = Movie(
    id = id,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    genres = genreIds.mapNotNull(genresById::get).map(RemoteGenre::toDomain),
    releaseDate = releaseDate.toLocalDateOrNull(),
    originalLanguage = originalLanguage,
    popularity = popularity,
    rating = rating,
    voteCount = voteCount,
    isAdult = isAdult,
)

internal fun RemoteMovieDetails.toDomain(): MovieDetails = MovieDetails(
    id = id,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    tagline = tagline,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate.toLocalDateOrNull(),
    runtimeMinutes = runtimeMinutes,
    status = status,
    genres = genres.map(RemoteGenre::toDomain),
    originalLanguage = originalLanguage,
    popularity = popularity,
    rating = rating,
    voteCount = voteCount,
    isAdult = isAdult,
    budget = budget,
    revenue = revenue,
    homepageUrl = homepageUrl,
    imdbId = imdbId,
)

internal fun Movie.toCached(): CachedMovie = CachedMovie(
    id = id,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    genres = genres.map { genre -> CachedGenre(genre.id, genre.name) },
    releaseDateEpochDay = releaseDate?.toEpochDay(),
    originalLanguage = originalLanguage,
    popularity = popularity,
    rating = rating,
    voteCount = voteCount,
    isAdult = isAdult,
)

internal fun CachedMovie.toDomain(): Movie = Movie(
    id = id,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    genres = genres.map { genre -> Genre(genre.id, genre.name) },
    releaseDate = releaseDateEpochDay?.let(LocalDate::ofEpochDay),
    originalLanguage = originalLanguage,
    popularity = popularity,
    rating = rating,
    voteCount = voteCount,
    isAdult = isAdult,
)

private fun String.toLocalDateOrNull(): LocalDate? {
    if (isBlank()) return null
    return try {
        LocalDate.parse(this)
    } catch (_: DateTimeParseException) {
        null
    }
}
