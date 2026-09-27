package com.mortex.amromovies.core.domain.model

import java.time.LocalDate

data class Movie(
    val id: Long,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val genres: List<Genre>,
    val releaseDate: LocalDate?,
    val originalLanguage: String,
    val popularity: Double,
    val rating: Double,
    val voteCount: Int,
    val isAdult: Boolean,
)
