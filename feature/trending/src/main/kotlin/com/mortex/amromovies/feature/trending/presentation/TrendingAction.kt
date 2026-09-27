package com.mortex.amromovies.feature.trending.presentation

import com.mortex.amromovies.feature.trending.presentation.model.MovieSort
import com.mortex.amromovies.feature.trending.presentation.model.SortDirection

sealed interface TrendingAction {
    data class SelectGenre(val genreId: Int?) : TrendingAction

    data class SelectSort(val sort: MovieSort) : TrendingAction

    data class SelectSortDirection(val direction: SortDirection) : TrendingAction

    data object ToggleSortDirection : TrendingAction

    data object Refresh : TrendingAction

    data object Retry : TrendingAction
}
