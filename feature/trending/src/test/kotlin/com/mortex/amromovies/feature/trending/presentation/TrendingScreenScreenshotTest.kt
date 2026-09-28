package com.mortex.amromovies.feature.trending.presentation

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.mortex.amromovies.core.designsystem.theme.AmroMoviesTheme
import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [35], qualifiers = "en-rUS-w393dp-h851dp-440dpi")
class TrendingScreenScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun loadedMovies() {
        composeRule.setContent {
            AmroMoviesTheme(darkTheme = false, dynamicColor = false) {
                TrendingScreen(
                    uiState = TrendingUiState(
                        movies = persistentListOf(
                            movie(
                                id = 1,
                                title = "The Last Horizon",
                                genres = persistentListOf(
                                    GenreUiModel(12, "Adventure"),
                                    GenreUiModel(18, "Drama"),
                                ),
                                releaseDate = LocalDate.of(2026, 9, 18),
                                releaseDateLabel = "Sep 18, 2026",
                                rating = 8.2,
                            ),
                            movie(
                                id = 2,
                                title = "Midnight Signal",
                                genres = persistentListOf(GenreUiModel(878, "Science Fiction")),
                                releaseDate = LocalDate.of(2026, 8, 7),
                                releaseDateLabel = "Aug 7, 2026",
                                rating = 7.6,
                            ),
                        ),
                        genres = persistentListOf(
                            GenreUiModel(12, "Adventure"),
                            GenreUiModel(18, "Drama"),
                            GenreUiModel(878, "Science Fiction"),
                        ),
                        hasUnfilteredMovies = true,
                        isLoading = false,
                    ),
                    onAction = {},
                    onMovieClick = {},
                )
            }
        }

        composeRule.onRoot().captureRoboImage()
    }

    private fun movie(
        id: Long,
        title: String,
        genres: kotlinx.collections.immutable.ImmutableList<GenreUiModel>,
        releaseDate: LocalDate,
        releaseDateLabel: String,
        rating: Double,
    ) = MovieUiModel(
        id = id,
        title = title,
        overview = "A deterministic screenshot fixture.",
        posterUrl = null,
        genres = genres,
        releaseDate = releaseDate,
        popularity = 100.0 - id,
        rating = rating,
        releaseDateLabel = releaseDateLabel,
    )
}
