package com.mortex.amromovies.core.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class MovieDetailsDto(
    val id: Long,
    val title: String,
    @SerialName("original_title") val originalTitle: String,
    val overview: String,
    val tagline: String = "",
    @SerialName("poster_path") val posterPath: String? = null,
    @SerialName("backdrop_path") val backdropPath: String? = null,
    @SerialName("release_date") val releaseDate: String = "",
    val runtime: Int? = null,
    val status: String,
    val genres: List<GenreDto> = emptyList(),
    @SerialName("original_language") val originalLanguage: String,
    val popularity: Double,
    @SerialName("vote_average") val voteAverage: Double,
    @SerialName("vote_count") val voteCount: Int,
    val adult: Boolean,
    val video: Boolean,
    val budget: Long,
    val revenue: Long,
    val homepage: String? = null,
    @SerialName("imdb_id") val imdbId: String? = null,
)
