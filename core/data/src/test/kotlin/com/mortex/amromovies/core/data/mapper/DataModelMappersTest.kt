package com.mortex.amromovies.core.data.mapper

import com.mortex.amromovies.core.data.source.model.CachedGenre
import com.mortex.amromovies.core.data.source.model.CachedMovie
import com.mortex.amromovies.core.data.source.model.RemoteGenre
import com.mortex.amromovies.core.data.source.model.RemoteMovie
import com.mortex.amromovies.core.data.source.model.RemoteMovieDetails
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate

class DataModelMappersTest {

    @Test
    fun `remote movie resolves known genres and parses valid release date`() {
        val movie = remoteMovie(
            genreIds = listOf(28, 999, 18),
            releaseDate = "2025-06-13",
        ).toDomain(
            genresById = listOf(
                RemoteGenre(18, "Drama"),
                RemoteGenre(28, "Action"),
            ).associateBy(RemoteGenre::id),
        )

        assertEquals(listOf("Action", "Drama"), movie.genres.map { it.name })
        assertEquals(LocalDate.of(2025, 6, 13), movie.releaseDate)
    }

    @Test
    fun `invalid remote dates map to null at data-domain boundary`() {
        val movie = remoteMovie(releaseDate = "not-a-date").toDomain(emptyMap())
        val details = remoteDetails(releaseDate = "").toDomain()

        assertNull(movie.releaseDate)
        assertNull(details.releaseDate)
    }

    @Test
    fun `cached movie maps to domain and domain maps back without persistence leakage`() {
        val cached = CachedMovie(
            id = 1,
            title = "Title",
            originalTitle = "Original",
            overview = "Overview",
            posterPath = null,
            backdropPath = null,
            genres = listOf(CachedGenre(18, "Drama")),
            releaseDateEpochDay = LocalDate.of(2025, 1, 2).toEpochDay(),
            originalLanguage = "en",
            popularity = 10.0,
            rating = 8.0,
            voteCount = 100,
            isAdult = false,
        )

        val domain = cached.toDomain()
        val roundTrip = domain.toCached()

        assertEquals(LocalDate.of(2025, 1, 2), domain.releaseDate)
        assertEquals(cached, roundTrip)
    }

    private fun remoteMovie(
        genreIds: List<Int> = emptyList(),
        releaseDate: String = "",
    ) = RemoteMovie(
        id = 1,
        title = "Title",
        originalTitle = "Original",
        overview = "Overview",
        posterPath = null,
        backdropPath = null,
        genreIds = genreIds,
        releaseDate = releaseDate,
        originalLanguage = "en",
        popularity = 10.0,
        rating = 8.0,
        voteCount = 100,
        isAdult = false,
    )

    private fun remoteDetails(releaseDate: String) = RemoteMovieDetails(
        id = 1,
        title = "Title",
        originalTitle = "Original",
        overview = "Overview",
        tagline = "Tagline",
        posterPath = null,
        backdropPath = null,
        releaseDate = releaseDate,
        runtimeMinutes = 120,
        status = "Released",
        genres = emptyList(),
        originalLanguage = "en",
        popularity = 10.0,
        rating = 8.0,
        voteCount = 100,
        isAdult = false,
        budget = 1,
        revenue = 2,
        homepageUrl = null,
        imdbId = null,
    )
}
