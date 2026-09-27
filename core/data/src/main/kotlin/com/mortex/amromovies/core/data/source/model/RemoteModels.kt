package com.mortex.amromovies.core.data.source.model

data class RemoteGenre(
    val id: Int,
    val name: String,
)

data class RemoteMovie(
    val id: Long,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val genreIds: List<Int>,
    val releaseDate: String,
    val originalLanguage: String,
    val popularity: Double,
    val rating: Double,
    val voteCount: Int,
    val isAdult: Boolean,
)

data class RemoteTrendingMoviesPage(
    val page: Int,
    val totalPages: Int,
    val movies: List<RemoteMovie>,
)

data class RemoteMovieDetails(
    val id: Long,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val tagline: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDate: String,
    val runtimeMinutes: Int?,
    val status: String,
    val genres: List<RemoteGenre>,
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
