package com.mortex.amromovies.feature.moviedetail.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mortex.amromovies.core.designsystem.theme.AmroMoviesTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MovieDetailOfflineInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loading_thenNoInternetError_displaysRetryAndEmitsRetryAction() {
        var uiState by mutableStateOf(MovieDetailUiState(isLoading = true))
        val actions = mutableListOf<MovieDetailAction>()

        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                MovieDetailScreen(
                    uiState = uiState,
                    onAction = actions::add,
                    onBackClick = {},
                    onOpenImdb = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Loading movie details").assertIsDisplayed()

        composeRule.runOnIdle {
            uiState = MovieDetailUiState(
                isLoading = false,
                error = MovieDetailUiError.NO_INTERNET,
            )
        }

        composeRule.onNodeWithText(
            "You appear to be offline. Check your connection and try again.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(MovieDetailAction.Retry), actions)
        }
    }
}
