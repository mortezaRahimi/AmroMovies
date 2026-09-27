package com.mortex.amromovies.di

import com.mortex.amromovies.feature.moviedetail.di.movieDetailModule
import com.mortex.amromovies.feature.trending.di.trendingModule
import org.koin.core.module.Module

val appModules: List<Module> = listOf(
    networkModule,
    databaseModule,
    repositoryModule,
    trendingModule,
    movieDetailModule,
)
