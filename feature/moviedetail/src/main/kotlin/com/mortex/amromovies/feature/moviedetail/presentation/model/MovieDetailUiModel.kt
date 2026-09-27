package com.mortex.amromovies.feature.moviedetail.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

@Immutable
data class MovieDetailUiModel(
    val id: Long,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val tagline: String?,
    val overview: String,
    val voteAverage: Double,
    val voteCount: Int,
    val budget: String?,
    val revenue: String?,
    val status: String,
    val imdbUrl: String?,
    val runtime: String?,
    val releaseDate: String?,
    val genres: ImmutableList<String>,
)
