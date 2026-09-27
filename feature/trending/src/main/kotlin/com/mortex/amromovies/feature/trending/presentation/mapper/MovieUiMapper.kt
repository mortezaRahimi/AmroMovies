package com.mortex.amromovies.feature.trending.presentation.mapper

import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import kotlinx.collections.immutable.toPersistentList

internal fun Movie.toUiModel(): MovieUiModel = MovieUiModel(
    id = id,
    title = title,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    genres = genres
        .map { genre -> GenreUiModel(id = genre.id, name = genre.name) }
        .toPersistentList(),
    releaseDate = releaseDate,
    popularity = popularity,
    rating = rating,
)
