package com.mortex.amromovies.core.network

import kotlinx.coroutines.test.runTest
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TmdbNetworkClientTest {
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `weekly trending request is authenticated and deserialized`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {
                  "page": 1,
                  "results": [{
                    "id": 550,
                    "title": "Example",
                    "original_title": "Example",
                    "overview": "Overview",
                    "poster_path": null,
                    "backdrop_path": "/backdrop.jpg",
                    "genre_ids": [18],
                    "release_date": "2025-01-01",
                    "original_language": "en",
                    "popularity": 12.5,
                    "vote_average": 8.0,
                    "vote_count": 42,
                    "adult": false,
                    "video": false,
                    "unknown_future_field": true
                  }],
                  "total_pages": 1,
                  "total_results": 1
                }
                """.trimIndent(),
            ),
        )
        val client = createClient()

        val response = client.api.getWeeklyTrendingMovies()
        val request = server.takeRequest()

        assertEquals("/3/trending/movie/week?language=en-US", request.path)
        assertEquals("Bearer test-token", request.getHeader("Authorization"))
        assertEquals("application/json", request.getHeader("Accept"))
        assertEquals(550L, response.results.single().id)
        assertNull(response.results.single().posterPath)
    }

    @Test
    fun `genre list endpoint returns genres`() = runTest {
        server.enqueue(jsonResponse("""{"genres":[{"id":28,"name":"Action"}]}"""))

        val response = createClient().api.getMovieGenres(language = "nl-NL")
        val request = server.takeRequest()

        assertEquals("/3/genre/movie/list?language=nl-NL", request.path)
        assertEquals("Action", response.genres.single().name)
    }

    @Test
    fun `movie details endpoint inserts movie id and deserializes details`() = runTest {
        server.enqueue(
            jsonResponse(
                """
                {
                  "id": 11,
                  "title": "Star Wars",
                  "original_title": "Star Wars",
                  "overview": "A long time ago",
                  "tagline": "May the Force be with you",
                  "poster_path": "/poster.jpg",
                  "backdrop_path": null,
                  "release_date": "1977-05-25",
                  "runtime": 121,
                  "status": "Released",
                  "genres": [{"id": 878, "name": "Science Fiction"}],
                  "original_language": "en",
                  "popularity": 100.0,
                  "vote_average": 8.2,
                  "vote_count": 20000,
                  "adult": false,
                  "video": false,
                  "budget": 11000000,
                  "revenue": 775398007,
                  "homepage": null,
                  "imdb_id": "tt0076759"
                }
                """.trimIndent(),
            ),
        )

        val response = createClient().api.getMovieDetails(movieId = 11)
        val request = server.takeRequest()

        assertEquals("/3/movie/11?language=en-US", request.path)
        assertEquals(121, response.runtime)
        assertEquals("Science Fiction", response.genres.single().name)
    }

    @Test
    fun `logging interceptor is absent when logging is disabled`() {
        val client = createClient(enableHttpLogging = false)

        assertFalse(client.okHttpClient.interceptors.any { it is HttpLoggingInterceptor })
    }

    private fun createClient(enableHttpLogging: Boolean = false) = TmdbNetworkClient(
        accessToken = "test-token",
        enableHttpLogging = enableHttpLogging,
        baseUrl = server.url("/3/").toString(),
    )

    private fun jsonResponse(body: String) = MockResponse()
        .setResponseCode(200)
        .setHeader("Content-Type", "application/json")
        .setBody(body)
}
