package com.mortex.amromovies.core.domain.model

import java.time.LocalDate

data class MovieDetails(
    val id: Long,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val tagline: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: LocalDate?,
    val runtimeMinutes: Int?,
    val status: String,
    val genres: List<Genre>,
    val originalLanguage: String,
    val popularity: Double,
    val rating: Double,
    val voteCount: Int,
    val isAdult: Boolean,
    val budget: Long,
    val revenue: Long,
    val homepageUrl: String?,
    val imdbId: String?,
)
