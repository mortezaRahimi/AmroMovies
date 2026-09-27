package com.mortex.amromovies

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.mortex.amromovies.core.designsystem.theme.AmroMoviesTheme
import com.mortex.amromovies.feature.trending.presentation.TrendingRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AmroMoviesTheme {
                TrendingRoute(
                    onMovieClick = { /* Movie details navigation is added with that feature. */ },
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
