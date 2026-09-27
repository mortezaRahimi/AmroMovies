package com.mortex.amromovies.core.network.mapper

import com.mortex.amromovies.core.network.model.GenreDto
import com.mortex.amromovies.core.network.model.MovieDetailsDto
import com.mortex.amromovies.core.network.model.TrendingMovieDto
import com.mortex.amromovies.core.network.model.TrendingMoviesResponseDto
import org.junit.Assert.assertEquals
import org.junit.Test

class TmdbDtoMappersTest {
    @Test
    fun `trending mapper preserves API ordering and genre ids`() {
        val response = TrendingMoviesResponseDto(
            page = 1,
            results = listOf(trendingMovie(genreIds = listOf(28, 999, 18))),
            totalPages = 1,
            totalResults = 1,
        )

        val page = response.toRemote()

        assertEquals(1, page.page)
        assertEquals(listOf(28, 999, 18), page.movies.single().genreIds)
    }

    @Test
    fun `trending mapper keeps release date as transport data`() {
        val response = TrendingMoviesResponseDto(
            page = 1,
            results = listOf(trendingMovie(releaseDate = "2025-06-13")),
            totalPages = 1,
            totalResults = 1,
        )

        val movie = response.toRemote().movies.single()

        assertEquals("2025-06-13", movie.releaseDate)
    }

    @Test
    fun `network mapper does not apply domain date policy`() {
        val trendingResponse = TrendingMoviesResponseDto(
            page = 1,
            results = listOf(trendingMovie(releaseDate = "not-a-date")),
            totalPages = 1,
            totalResults = 1,
        )
        val details = movieDetails(releaseDate = "")

        assertEquals("not-a-date", trendingResponse.toRemote().movies.single().releaseDate)
        assertEquals("", details.toRemote().releaseDate)
    }

    @Test
    fun `movie details mapper preserves detail fields and genres`() {
        val details = movieDetails(releaseDate = "1977-05-25")

        val movie = details.toRemote()

        assertEquals(121, movie.runtimeMinutes)
        assertEquals("Science Fiction", movie.genres.single().name)
        assertEquals("1977-05-25", movie.releaseDate)
        assertEquals("tt0076759", movie.imdbId)
    }

    private fun trendingMovie(
        genreIds: List<Int> = emptyList(),
        releaseDate: String = "",
    ) = TrendingMovieDto(
        id = 1,
        title = "Title",
        originalTitle = "Original title",
        overview = "Overview",
        posterPath = "/poster.jpg",
        backdropPath = null,
        genreIds = genreIds,
        releaseDate = releaseDate,
        originalLanguage = "en",
        popularity = 10.0,
        voteAverage = 8.0,
        voteCount = 100,
        adult = false,
        video = false,
    )

    private fun movieDetails(releaseDate: String) = MovieDetailsDto(
        id = 11,
        title = "Star Wars",
        originalTitle = "Star Wars",
        overview = "Overview",
        tagline = "Tagline",
        posterPath = "/poster.jpg",
        backdropPath = null,
        releaseDate = releaseDate,
        runtime = 121,
        status = "Released",
        genres = listOf(GenreDto(id = 878, name = "Science Fiction")),
        originalLanguage = "en",
        popularity = 100.0,
        voteAverage = 8.2,
        voteCount = 20_000,
        adult = false,
        video = false,
        budget = 11_000_000,
        revenue = 775_398_007,
        homepage = null,
        imdbId = "tt0076759",
    )
}
