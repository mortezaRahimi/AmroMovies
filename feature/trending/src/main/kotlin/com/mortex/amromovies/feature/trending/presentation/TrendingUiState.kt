package com.mortex.amromovies.feature.trending.presentation

import androidx.compose.runtime.Immutable
import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieSort
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import com.mortex.amromovies.feature.trending.presentation.model.SortDirection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class TrendingUiState(
    val movies: ImmutableList<MovieUiModel> = persistentListOf(),
    val genres: ImmutableList<GenreUiModel> = persistentListOf(),
    val selectedGenreId: Int? = null,
    val sortOption: MovieSort = MovieSort.POPULARITY,
    val sortDirection: SortDirection = SortDirection.DESCENDING,
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: TrendingUiError? = null,
)

enum class TrendingUiError {
    NO_INTERNET,
    UNAUTHORIZED,
    SERVER,
    NOT_FOUND,
    UNKNOWN,
}
