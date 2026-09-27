package com.mortex.amromovies.feature.moviedetail.di

import com.mortex.amromovies.feature.moviedetail.presentation.MovieDetailViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val movieDetailModule = module {
    viewModel { parameters ->
        MovieDetailViewModel(
            movieId = parameters.get(),
            moviesRepository = get(),
        )
    }
}
