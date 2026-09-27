package com.mortex.amromovies.core.data.source.model

data class CachedGenre(
    val id: Int,
    val name: String,
)

data class CachedMovie(
    val id: Long,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val genres: List<CachedGenre>,
    val releaseDateEpochDay: Long?,
    val originalLanguage: String,
    val popularity: Double,
    val rating: Double,
    val voteCount: Int,
    val isAdult: Boolean,
)
