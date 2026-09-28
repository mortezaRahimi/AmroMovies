package com.mortex.amromovies.feature.trending.presentation.mapper

import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import kotlinx.collections.immutable.toPersistentList
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

internal fun Movie.toUiModel(locale: Locale = Locale.getDefault()): MovieUiModel {
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)
    return MovieUiModel(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterPath?.let { path -> "$TMDB_POSTER_BASE_URL$path" },
        genres = genres
            .map { genre -> GenreUiModel(id = genre.id, name = genre.name) }
            .toPersistentList(),
        releaseDate = releaseDate,
        popularity = popularity,
        rating = rating,
        releaseDateLabel = releaseDate?.format(dateFormatter),
    )
}

private const val TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342"
