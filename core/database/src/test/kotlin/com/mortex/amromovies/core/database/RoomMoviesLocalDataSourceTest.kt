package com.mortex.amromovies.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.mortex.amromovies.core.domain.model.Genre
import com.mortex.amromovies.core.domain.model.Movie
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class RoomMoviesLocalDataSourceTest {
    private lateinit var database: AmroMoviesDatabase
    private lateinit var localDataSource: MoviesLocalDataSource

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AmroMoviesDatabase::class.java,
        ).allowMainThreadQueries().build()
        localDataSource = RoomMoviesLocalDataSource(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `replacement preserves supplied trending rank and genre relationships`() = runTest {
        val movies = listOf(movie(30, "Comedy"), movie(10, "Drama"), movie(20, "Action"))

        localDataSource.replaceWeeklyTrendingMovies(movies, refreshedAtEpochMillis = 1234L)

        val cached = localDataSource.observeWeeklyTrendingMovies().first()
        assertEquals(listOf(30L, 10L, 20L), cached.map(Movie::id))
        assertEquals(listOf("Comedy", "Drama", "Action"), cached.map { it.genres.single().name })
        assertEquals(LocalDate.of(2025, 1, 2), cached.first().releaseDate)
        assertEquals(1234L, localDataSource.getWeeklyTrendingUpdatedAt())
    }

    @Test
    fun `replacement removes stale trending movies atomically`() = runTest {
        localDataSource.replaceWeeklyTrendingMovies(
            movies = listOf(movie(1, "Drama"), movie(2, "Action"), movie(3, "Comedy")),
            refreshedAtEpochMillis = 100L,
        )

        localDataSource.replaceWeeklyTrendingMovies(
            movies = listOf(movie(3, "Comedy"), movie(4, "Family")),
            refreshedAtEpochMillis = 200L,
        )

        val cached = localDataSource.observeWeeklyTrendingMovies().first()
        assertEquals(listOf(3L, 4L), cached.map(Movie::id))
        assertEquals(200L, localDataSource.getWeeklyTrendingUpdatedAt())
    }

    private fun movie(id: Long, genreName: String) = Movie(
        id = id,
        title = "Movie $id",
        originalTitle = "Original $id",
        overview = "Overview $id",
        posterPath = "/$id.jpg",
        backdropPath = null,
        genres = listOf(Genre(id = id.toInt(), name = genreName)),
        releaseDate = LocalDate.of(2025, 1, 2),
        originalLanguage = "en",
        popularity = id.toDouble(),
        rating = 7.5,
        voteCount = id.toInt(),
        isAdult = false,
    )
}
