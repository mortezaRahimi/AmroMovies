package com.mortex.amromovies.feature.trending.presentation.mapper

import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate
import java.util.Locale

class MovieUiMapperTest {

    @Test
    fun `domain movie maps to presentation model`() {
        val movie = Movie(
            id = 42,
            title = "Title",
            originalTitle = "Original title",
            overview = "Overview",
            posterPath = "/poster.jpg",
            backdropPath = "/backdrop.jpg",
            genres = listOf(Genre(id = 28, name = "Action")),
            releaseDate = LocalDate.of(2025, 6, 1),
            originalLanguage = "en",
            popularity = 123.4,
            rating = 7.8,
            voteCount = 500,
            isAdult = false,
        )

        val result = movie.toUiModel(Locale.US)

        assertEquals(movie.id, result.id)
        assertEquals(movie.title, result.title)
        assertEquals(movie.overview, result.overview)
        assertEquals("https://image.tmdb.org/t/p/w342/poster.jpg", result.posterUrl)
        assertEquals(listOf(GenreUiModel(28, "Action")), result.genres)
        assertEquals(movie.releaseDate, result.releaseDate)
        assertEquals("Jun 1, 2025", result.releaseDateLabel)
        assertEquals(movie.popularity, result.popularity, 0.0)
        assertEquals(movie.rating, result.rating, 0.0)
    }
}
