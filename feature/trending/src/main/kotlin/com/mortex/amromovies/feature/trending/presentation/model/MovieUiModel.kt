package com.mortex.amromovies.feature.trending.presentation.model

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate

@Immutable
data class MovieUiModel(
    val id: Long,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val genres: ImmutableList<GenreUiModel>,
    val releaseDate: LocalDate?,
    val popularity: Double,
    val rating: Double,
)
