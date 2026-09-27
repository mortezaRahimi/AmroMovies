package com.mortex.amromovies.feature.moviedetail.presentation.mapper

import com.mortex.amromovies.feature.moviedetail.presentation.testMovieDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.Locale

class MovieDetailUiMapperTest {

    @Test
    fun `maps complete domain details to display-ready UI model`() {
        val result = testMovieDetails().toUiModel(Locale.US)

        assertEquals("Example Movie", result.title)
        assertEquals("https://image.tmdb.org/t/p/w500/poster.jpg", result.posterUrl)
        assertEquals("https://image.tmdb.org/t/p/w780/backdrop.jpg", result.backdropUrl)
        assertEquals("A great tagline", result.tagline)
        assertEquals("2h 5m", result.runtime)
        assertEquals("Mar 4, 2025", result.releaseDate)
        assertEquals("$100,000,000", result.budget)
        assertEquals("$250,000,000", result.revenue)
        assertEquals("https://www.imdb.com/title/tt1234567", result.imdbUrl)
        assertEquals(listOf("Action", "Drama"), result.genres)
    }

    @Test
    fun `missing and zero values map to intentional absence`() {
        val result = testMovieDetails(
            tagline = "   ",
            runtimeMinutes = 0,
            releaseDate = null,
            budget = 0,
            revenue = 0,
            imdbId = "invalid-id",
            posterPath = " ",
            backdropPath = null,
        ).toUiModel(Locale.US)

        assertNull(result.tagline)
        assertNull(result.runtime)
        assertNull(result.releaseDate)
        assertNull(result.budget)
        assertNull(result.revenue)
        assertNull(result.imdbUrl)
        assertNull(result.posterUrl)
        assertNull(result.backdropUrl)
    }

    @Test
    fun `runtime formats exact hours and minutes-only values`() {
        assertEquals("2h", testMovieDetails(runtimeMinutes = 120).toUiModel(Locale.US).runtime)
        assertEquals("45m", testMovieDetails(runtimeMinutes = 45).toUiModel(Locale.US).runtime)
    }
}
