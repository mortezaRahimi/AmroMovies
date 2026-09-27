package com.mortex.amromovies.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mortex.amromovies.feature.moviedetail.presentation.MovieDetailRoute
import com.mortex.amromovies.feature.trending.presentation.TrendingRoute

@Composable
fun AmroMoviesNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = TRENDING_ROUTE,
        modifier = modifier,
    ) {
        composable(TRENDING_ROUTE) {
            TrendingRoute(
                onMovieClick = { movieId ->
                    navController.navigate(movieDetailRoute(movieId))
                },
            )
        }
        composable(
            route = MOVIE_DETAIL_ROUTE,
            arguments = listOf(
                navArgument(MOVIE_ID_ARGUMENT) { type = NavType.LongType },
            ),
        ) { backStackEntry ->
            val movieId = requireNotNull(backStackEntry.arguments?.getLong(MOVIE_ID_ARGUMENT))
            MovieDetailRoute(
                movieId = movieId,
                onBackClick = navController::navigateUp,
            )
        }
    }
}

internal fun movieDetailRoute(movieId: Long): String = "movie/$movieId"

private const val TRENDING_ROUTE = "trending"
private const val MOVIE_ID_ARGUMENT = "movieId"
private const val MOVIE_DETAIL_ROUTE = "movie/{$MOVIE_ID_ARGUMENT}"
