package com.mortex.amromovies.feature.trending.presentation.mapper

import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import kotlinx.collections.immutable.toPersistentList

internal fun Movie.toUiModel(): MovieUiModel = MovieUiModel(
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
)

private const val TMDB_POSTER_BASE_URL = "https://image.tmdb.org/t/p/w342"
