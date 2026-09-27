package com.mortex.amromovies.feature.trending.presentation

import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieSort
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import com.mortex.amromovies.feature.trending.presentation.model.SortDirection
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TrendingTransformationsTest {

    @Test
    fun `all sorting and direction combinations produce the expected order`() {
        val expectedOrders = mapOf(
            MovieSort.POPULARITY to mapOf(
                SortDirection.ASCENDING to listOf(2L, 4L, 1L, 3L),
                SortDirection.DESCENDING to listOf(3L, 1L, 4L, 2L),
            ),
            MovieSort.TITLE to mapOf(
                SortDirection.ASCENDING to listOf(2L, 4L, 1L, 3L),
                SortDirection.DESCENDING to listOf(3L, 1L, 4L, 2L),
            ),
            MovieSort.RELEASE_DATE to mapOf(
                SortDirection.ASCENDING to listOf(3L, 1L, 4L, 2L),
                SortDirection.DESCENDING to listOf(4L, 1L, 3L, 2L),
            ),
        )

        expectedOrders.forEach { (sort, directions) ->
            directions.forEach { (direction, expectedIds) ->
                val result = filterAndSortMovies(movies, null, sort, direction)

                assertEquals("$sort $direction", expectedIds, result.map(MovieUiModel::id))
            }
        }
    }

    @Test
    fun `filtering combines correctly with every sorting and direction option`() {
        val expectedOrders = mapOf(
            MovieSort.POPULARITY to mapOf(
                SortDirection.ASCENDING to listOf(1L, 3L),
                SortDirection.DESCENDING to listOf(3L, 1L),
            ),
            MovieSort.TITLE to mapOf(
                SortDirection.ASCENDING to listOf(1L, 3L),
                SortDirection.DESCENDING to listOf(3L, 1L),
            ),
            MovieSort.RELEASE_DATE to mapOf(
                SortDirection.ASCENDING to listOf(3L, 1L),
                SortDirection.DESCENDING to listOf(1L, 3L),
            ),
        )

        expectedOrders.forEach { (sort, directions) ->
            directions.forEach { (direction, expectedIds) ->
                val result = filterAndSortMovies(movies, GENRE_ONE.id, sort, direction)

                assertEquals("$sort $direction", expectedIds, result.map(MovieUiModel::id))
            }
        }
    }

    @Test
    fun `missing release dates are always last in deterministic order`() {
        val moviesWithMissingDates = listOf(
            movie(id = 1, title = "Zulu", releaseDate = null),
            movie(id = 2, title = "alpha", releaseDate = null),
            movie(id = 3, title = "Dated", releaseDate = LocalDate.of(2020, 1, 1)),
        )

        SortDirection.entries.forEach { direction ->
            val result = filterAndSortMovies(
                moviesWithMissingDates,
                selectedGenreId = null,
                sort = MovieSort.RELEASE_DATE,
                direction = direction,
            )

            assertEquals(listOf(3L, 2L, 1L), result.map(MovieUiModel::id))
        }
    }

    @Test
    fun `filtering is restricted to the existing top one hundred`() {
        val source = (1L..101L).map { id ->
            movie(
                id = id,
                title = "Movie $id",
                genres = if (id == 101L) persistentListOf(GENRE_ONE) else persistentListOf(GENRE_TWO),
            )
        }

        val result = filterAndSortMovies(
            source,
            selectedGenreId = GENRE_ONE.id,
            sort = MovieSort.POPULARITY,
            direction = SortDirection.DESCENDING,
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun `transformations do not mutate the source list`() {
        val mutableSource = movies.toMutableList()
        val original = mutableSource.toList()

        filterAndSortMovies(
            mutableSource,
            selectedGenreId = GENRE_ONE.id,
            sort = MovieSort.TITLE,
            direction = SortDirection.DESCENDING,
        )

        assertEquals(original, mutableSource)
    }

    @Test
    fun `available genres are distinct sorted and restricted to top one hundred`() {
        val source = List(100) { index ->
            movie(
                id = index.toLong(),
                genres = persistentListOf(GENRE_TWO, GENRE_ONE),
            )
        } + movie(id = 101, genres = persistentListOf(GenreUiModel(3, "Hidden")))

        assertEquals(listOf(GENRE_ONE, GENRE_TWO), availableGenres(source))
    }

    private val movies = listOf(
        movie(1, "beta", popularity = 20.0, releaseDate = LocalDate.of(2023, 1, 1), genres = persistentListOf(GENRE_ONE)),
        movie(2, "Alpha", popularity = 10.0, releaseDate = null, genres = persistentListOf(GENRE_TWO)),
        movie(3, "charlie", popularity = 30.0, releaseDate = LocalDate.of(2021, 1, 1), genres = persistentListOf(GENRE_ONE, GENRE_TWO)),
        movie(4, "alpha", popularity = 15.0, releaseDate = LocalDate.of(2024, 1, 1), genres = persistentListOf(GENRE_TWO)),
    )

    private fun movie(
        id: Long,
        title: String = "Movie $id",
        popularity: Double = id.toDouble(),
        releaseDate: LocalDate? = LocalDate.of(2024, 1, 1),
        genres: kotlinx.collections.immutable.ImmutableList<GenreUiModel> = persistentListOf(),
    ) = MovieUiModel(
        id = id,
        title = title,
        overview = "Overview",
        posterUrl = null,
        genres = genres,
        releaseDate = releaseDate,
        popularity = popularity,
        rating = 8.0,
    )

    private companion object {
        val GENRE_ONE = GenreUiModel(1, "Action")
        val GENRE_TWO = GenreUiModel(2, "Comedy")
    }
}
