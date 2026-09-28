package com.mortex.amromovies.feature.trending.presentation

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
class TrendingOfflineInstrumentedTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loading_thenNoInternetError_displaysRetryAndEmitsRetryAction() {
        var uiState by mutableStateOf(TrendingUiState(isLoading = true))
        val actions = mutableListOf<TrendingAction>()

        composeRule.setContent {
            AmroMoviesTheme(dynamicColor = false) {
                TrendingScreen(
                    uiState = uiState,
                    onAction = actions::add,
                    onMovieClick = {},
                )
            }
        }

        composeRule.onNodeWithContentDescription("Loading trending movies").assertIsDisplayed()

        composeRule.runOnIdle {
            uiState = TrendingUiState(
                isLoading = false,
                error = TrendingUiError.NO_INTERNET,
            )
        }

        composeRule.onNodeWithText(
            "You appear to be offline. Check your connection and try again.",
        ).assertIsDisplayed()
        composeRule.onNodeWithText("Retry").performClick()

        composeRule.runOnIdle {
            assertEquals(listOf(TrendingAction.Retry), actions)
        }
    }
}
