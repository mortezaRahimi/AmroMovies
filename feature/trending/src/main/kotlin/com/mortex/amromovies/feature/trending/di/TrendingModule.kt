package com.mortex.amromovies.feature.trending.di

import com.mortex.amromovies.feature.trending.presentation.TrendingViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val trendingModule = module {
    viewModel {
        TrendingViewModel(moviesRepository = get())
    }
}
