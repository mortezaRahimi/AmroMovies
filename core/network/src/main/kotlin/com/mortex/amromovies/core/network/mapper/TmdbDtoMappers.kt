package com.mortex.amromovies.core.network.mapper

import com.mortex.amromovies.core.data.source.model.RemoteGenre
import com.mortex.amromovies.core.data.source.model.RemoteMovie
import com.mortex.amromovies.core.data.source.model.RemoteMovieDetails
import com.mortex.amromovies.core.data.source.model.RemoteTrendingMoviesPage
import com.mortex.amromovies.core.network.model.GenreDto
import com.mortex.amromovies.core.network.model.GenreListResponseDto
import com.mortex.amromovies.core.network.model.MovieDetailsDto
import com.mortex.amromovies.core.network.model.TrendingMovieDto
import com.mortex.amromovies.core.network.model.TrendingMoviesResponseDto

internal fun GenreDto.toRemote(): RemoteGenre = RemoteGenre(
    id = id,
    name = name,
)

internal fun GenreListResponseDto.toRemote(): List<RemoteGenre> = genres.map(GenreDto::toRemote)

internal fun TrendingMoviesResponseDto.toRemote(): RemoteTrendingMoviesPage =
    RemoteTrendingMoviesPage(
        page = page,
        totalPages = totalPages,
        movies = results.map(TrendingMovieDto::toRemote),
    )

private fun TrendingMovieDto.toRemote(): RemoteMovie = RemoteMovie(
    id = id,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    posterPath = posterPath,
    backdropPath = backdropPath,
    genreIds = genreIds,
    releaseDate = releaseDate,
    originalLanguage = originalLanguage,
    popularity = popularity,
    rating = voteAverage,
    voteCount = voteCount,
    isAdult = adult,
)

internal fun MovieDetailsDto.toRemote(): RemoteMovieDetails = RemoteMovieDetails(
    id = id,
    title = title,
    originalTitle = originalTitle,
    overview = overview,
    tagline = tagline,
    posterPath = posterPath,
    backdropPath = backdropPath,
    releaseDate = releaseDate,
    runtimeMinutes = runtime,
    status = status,
    genres = genres.map(GenreDto::toRemote),
    originalLanguage = originalLanguage,
    popularity = popularity,
    rating = voteAverage,
    voteCount = voteCount,
    isAdult = adult,
    budget = budget,
    revenue = revenue,
    homepageUrl = homepage,
    imdbId = imdbId,
)
