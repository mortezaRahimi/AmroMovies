package com.mortex.amromovies.feature.moviedetail.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.mortex.amromovies.core.designsystem.theme.AmroMoviesTheme
import com.mortex.amromovies.feature.moviedetail.presentation.model.MovieDetailUiModel
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "en-rUS-w393dp-h851dp-440dpi")
class MovieDetailScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadedMovieDetails() {
        composeRule.setContent {
            AmroMoviesTheme(darkTheme = false, dynamicColor = false) {
                MovieDetailScreen(
                    uiState = MovieDetailUiState(
                        movie = MovieDetailUiModel(
                            id = 1,
                            title = "The Last Horizon",
                            posterUrl = null,
                            backdropUrl = null,
                            tagline = "Beyond the edge, everything changes.",
                            overview = "A crew crosses the final mapped boundary in search of a new home.",
                            voteAverage = 8.2,
                            voteCount = 12_450,
                            budget = "$120,000,000",
                            revenue = "$486,000,000",
                            status = "Released",
                            imdbUrl = "https://www.imdb.com/title/tt1234567",
                            runtime = "2h 14m",
                            releaseDate = "September 18, 2026",
                            genres = persistentListOf("Adventure", "Drama"),
                        ),
                        isLoading = false,
                    ),
                    onAction = {},
                    onBackClick = {},
                    onOpenImdb = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage()
    }
}
