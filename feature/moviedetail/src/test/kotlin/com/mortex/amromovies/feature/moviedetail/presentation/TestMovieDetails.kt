package com.mortex.amromovies.feature.moviedetail.presentation

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.MovieDetails
import java.time.LocalDate

internal fun testMovieDetails(
    tagline: String = "A great tagline",
    runtimeMinutes: Int? = 125,
    releaseDate: LocalDate? = LocalDate.of(2025, 3, 4),
    budget: Long = 100_000_000,
    revenue: Long = 250_000_000,
    imdbId: String? = "tt1234567",
    posterPath: String? = "/poster.jpg",
    backdropPath: String? = "/backdrop.jpg",
) = MovieDetails(
    id = 42,
    title = "Example Movie",
    originalTitle = "Example Movie",
    overview = "An example overview.",
    tagline = tagline,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtimeMinutes = runtimeMinutes,
    status = "Released",
    genres = listOf(Genre(28, "Action"), Genre(18, "Drama")),
    originalLanguage = "en",
    popularity = 100.0,
    rating = 8.2,
    voteCount = 1_234,
    isAdult = false,
    budget = budget,
    revenue = revenue,
    homepageUrl = null,
    imdbId = imdbId,
)
