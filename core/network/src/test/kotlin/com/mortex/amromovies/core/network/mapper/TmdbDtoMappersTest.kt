package com.mortex.amromovies.core.network.mapper

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.network.model.GenreDto
import com.mortex.amromovies.core.network.model.MovieDetailsDto
import com.mortex.amromovies.core.network.model.TrendingMovieDto
import com.mortex.amromovies.core.network.model.TrendingMoviesResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class TmdbDtoMappersTest {
    @Test
    fun `trending mapper resolves known genres and ignores unknown ids`() {
        val response = TrendingMoviesResponseDto(
            page = 1,
            results = listOf(trendingMovie(genreIds = listOf(28, 999, 18))),
            totalPages = 1,
            totalResults = 1,
        )

        val movies = response.toDomain(
            genres = listOf(
                Genre(id = 18, name = "Drama"),
                Genre(id = 28, name = "Action"),
            ),
        )

        assertEquals(listOf("Action", "Drama"), movies.single().genres.map(Genre::name))
    }

    @Test
    fun `trending mapper converts valid release date`() {
        val response = TrendingMoviesResponseDto(
            page = 1,
            results = listOf(trendingMovie(releaseDate = "2025-06-13")),
            totalPages = 1,
            totalResults = 1,
        )

        val movie = response.toDomain(genres = emptyList()).single()

        assertEquals(LocalDate.of(2025, 6, 13), movie.releaseDate)
    }

    @Test
    fun `invalid or absent release dates map to null`() {
        val trendingResponse = TrendingMoviesResponseDto(
            page = 1,
            results = listOf(trendingMovie(releaseDate = "not-a-date")),
            totalPages = 1,
            totalResults = 1,
        )
        val details = movieDetails(releaseDate = "")

        assertNull(trendingResponse.toDomain(emptyList()).single().releaseDate)
        assertNull(details.toDomain().releaseDate)
    }

    @Test
    fun `movie details mapper preserves detail fields and genres`() {
        val details = movieDetails(releaseDate = "1977-05-25")

        val movie = details.toDomain()

        assertEquals(121, movie.runtimeMinutes)
        assertEquals("Science Fiction", movie.genres.single().name)
        assertEquals(LocalDate.of(1977, 5, 25), movie.releaseDate)
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
