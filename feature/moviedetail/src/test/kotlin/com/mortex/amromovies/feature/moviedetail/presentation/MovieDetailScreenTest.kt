package com.mortex.amromovies.feature.moviedetail.presentation

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.mortex.amromovies.core.designsystem.theme.AmroMoviesTheme
import com.mortex.amromovies.feature.moviedetail.presentation.mapper.toUiModel
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Locale

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class MovieDetailScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `loading state is accessible`() {
        setContent(MovieDetailUiState(isLoading = true))

        composeRule.onNodeWithContentDescription("Loading movie details").assertIsDisplayed()
    }

    @Test
    fun `back icon is accessible and emits navigation callback`() {
        var backClicked = false
        setContent(
            state = MovieDetailUiState(isLoading = true),
            onBackClick = { backClicked = true },
        )

        composeRule.onNodeWithContentDescription("Back").performClick()

        assertEquals(true, backClicked)
    }

    @Test
    fun `error state displays message and emits retry`() {
        val actions = mutableListOf<MovieDetailAction>()
        setContent(
            state = MovieDetailUiState(
                isLoading = false,
                error = MovieDetailUiError.NO_INTERNET,
            ),
            onAction = actions::add,
        )

        composeRule.onNodeWithText(
            "You appear to be offline. Check your connection and try again.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(listOf(MovieDetailAction.Retry), actions)
    }

    @Test
    fun `complete details are displayed and IMDb link emits valid URL`() {
        val openedUrls = mutableListOf<String>()
        val movie = testMovieDetails(
            posterPath = null,
            backdropPath = null,
        ).toUiModel(Locale.US)
        setContent(
            state = MovieDetailUiState(movie = movie, isLoading = false),
            onOpenImdb = openedUrls::add,
        )

        composeRule.onNodeWithText("Example Movie").assertIsDisplayed()
        composeRule.onNodeWithText("A great tagline").assertIsDisplayed()
        composeRule.onNodeWithText("An example overview.").assertIsDisplayed()
        composeRule.onNodeWithText("$100,000,000").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithText("View on IMDb").performScrollTo().performClick()

        assertEquals(listOf("https://www.imdb.com/title/tt1234567"), openedUrls)
    }

    @Test
    fun `missing optional values show fallbacks and no IMDb action`() {
        val movie = testMovieDetails(
            tagline = "",
            runtimeMinutes = null,
            releaseDate = null,
            budget = 0,
            revenue = 0,
            imdbId = null,
            posterPath = null,
            backdropPath = null,
        ).toUiModel(Locale.US)
        setContent(MovieDetailUiState(movie = movie, isLoading = false))

        composeRule.onNodeWithText("No tagline available").assertIsDisplayed()
        composeRule.onAllNodesWithText("Not available").assertCountEquals(4)
        composeRule.onNodeWithText("View on IMDb").assertDoesNotExist()
    }

    private fun setContent(
        state: MovieDetailUiState,
        onAction: (MovieDetailAction) -> Unit = {},
        onBackClick: () -> Unit = {},
        onOpenImdb: (String) -> Unit = {},
    ) {
        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                MovieDetailScreen(
                    uiState = state,
                    onAction = onAction,
                    onBackClick = onBackClick,
                    onOpenImdb = onOpenImdb,
                )
            }
        }
    }
}
