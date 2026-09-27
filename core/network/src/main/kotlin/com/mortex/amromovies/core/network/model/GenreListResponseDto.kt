package com.mortex.amromovies.core.network.model

import kotlinx.serialization.Serializable

@Serializable
internal data class GenreListResponseDto(
    val genres: List<GenreDto>,
)

@Serializable
internal data class GenreDto(
    val id: Int,
    val name: String,
)
