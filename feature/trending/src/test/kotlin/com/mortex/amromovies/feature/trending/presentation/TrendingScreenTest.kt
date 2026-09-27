package com.mortex.amromovies.feature.trending.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToIndex
import com.mortex.amromovies.core.designsystem.theme.AmroMoviesTheme
import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieSort
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import com.mortex.amromovies.feature.trending.presentation.model.SortDirection
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class TrendingScreenTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `initial loading is displayed`() {
        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                TrendingScreen(
                    uiState = TrendingUiState(isLoading = true),
                    onAction = {},
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Trending this week").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Loading trending movies").assertIsDisplayed()
        composeRule.onNodeWithText("No trending movies are available.").assertDoesNotExist()
    }

    @Test
    fun `network error displays message and emits retry action`() {
        val actions = mutableListOf<TrendingAction>()
        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                TrendingScreen(
                    uiState = TrendingUiState(
                        isLoading = false,
                        error = TrendingUiError.NO_INTERNET,
                    ),
                    onAction = actions::add,
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithText(
            "You appear to be offline. Check your connection and try again.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        assertEquals(listOf(TrendingAction.Retry), actions)
    }

    @Test
    fun `refresh control emits refresh action`() {
        val actions = mutableListOf<TrendingAction>()
        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                TrendingScreen(
                    uiState = TrendingUiState(isLoading = false),
                    onAction = actions::add,
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Refresh").performClick()

        assertEquals(listOf(TrendingAction.Refresh), actions)
    }

    @Test
    fun `selected genre empty state is distinct from canonical empty state`() {
        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                TrendingScreen(
                    uiState = TrendingUiState(
                        isLoading = false,
                        selectedGenreId = 28,
                        hasUnfilteredMovies = true,
                    ),
                    onAction = {},
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithText("No movies match the selected genre.").assertIsDisplayed()
    }

    @Test
    fun `movie card displays title genre and missing poster state`() {
        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                TrendingScreen(
                    uiState = TrendingUiState(
                        movies = persistentListOf(
                            MovieUiModel(
                                id = 7,
                                title = "Example Movie",
                                overview = "Overview",
                                posterUrl = null,
                                genres = persistentListOf(GenreUiModel(28, "Action")),
                                releaseDate = null,
                                popularity = 10.0,
                                rating = 7.5,
                            ),
                        ),
                        hasUnfilteredMovies = true,
                        isLoading = false,
                    ),
                    onAction = {},
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithText("Example Movie").assertIsDisplayed()
        composeRule.onNodeWithText("Action").assertIsDisplayed()
        composeRule.onNodeWithText("Poster unavailable for Example Movie").assertIsDisplayed()
    }

    @Test
    fun `genre control opens picker and emits selected genre`() {
        val actions = mutableListOf<TrendingAction>()
        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                TrendingScreen(
                    uiState = TrendingUiState(
                        genres = persistentListOf(
                            GenreUiModel(28, "Action"),
                            GenreUiModel(35, "Comedy"),
                        ),
                        isLoading = false,
                    ),
                    onAction = actions::add,
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Genre: All").performClick()
        composeRule.onNodeWithText("Choose a genre").assertIsDisplayed()
        composeRule.onNodeWithText("Action").performClick()

        assertEquals(listOf(TrendingAction.SelectGenre(28)), actions)
    }

    @Test
    fun `sort control exposes criterion and direction in one picker`() {
        val actions = mutableListOf<TrendingAction>()
        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                TrendingScreen(
                    uiState = TrendingUiState(isLoading = false),
                    onAction = actions::add,
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Sort: Popularity · Descending").performClick()
        composeRule.onNodeWithText("Sort movies by").assertIsDisplayed()
        composeRule.onNodeWithText("Done").assertIsDisplayed()
        composeRule.onNodeWithText("Title").performClick()
        composeRule.onNode(hasScrollAction()).performScrollToIndex(4)
        composeRule.onNodeWithText("Ascending").performClick()

        assertEquals(
            listOf(
                TrendingAction.SelectSort(MovieSort.TITLE),
                TrendingAction.SelectSortDirection(SortDirection.ASCENDING),
            ),
            actions,
        )
    }
}
