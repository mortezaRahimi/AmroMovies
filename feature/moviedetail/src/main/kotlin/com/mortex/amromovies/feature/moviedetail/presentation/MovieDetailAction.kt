package com.mortex.amromovies.feature.moviedetail.presentation

sealed interface MovieDetailAction {
    data object Retry : MovieDetailAction
}
