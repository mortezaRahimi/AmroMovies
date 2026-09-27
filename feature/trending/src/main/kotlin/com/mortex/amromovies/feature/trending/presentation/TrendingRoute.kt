package com.mortex.amromovies.feature.trending.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel

@Composable
fun TrendingRoute(
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TrendingViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TrendingScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onMovieClick = onMovieClick,
        modifier = modifier,
    )
}
