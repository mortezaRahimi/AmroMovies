package com.mortex.amromovies.di

import com.mortex.amromovies.BuildConfig
import com.mortex.amromovies.core.common.time.AppClock
import com.mortex.amromovies.core.common.time.SystemAppClock
import com.mortex.amromovies.core.data.DefaultMoviesRepository
import com.mortex.amromovies.core.database.MoviesLocalDataSource
import com.mortex.amromovies.core.database.createMoviesLocalDataSource
import com.mortex.amromovies.core.domain.repository.MoviesRepository
import com.mortex.amromovies.core.network.TmdbNetworkClient
import com.mortex.amromovies.core.network.TmdbRemoteDataSource
import com.mortex.amromovies.feature.trending.di.trendingModule
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

private val applicationModule = module {
    single<TmdbRemoteDataSource> {
        TmdbNetworkClient(
            accessToken = BuildConfig.TMDB_ACCESS_TOKEN,
            enableHttpLogging = BuildConfig.DEBUG,
        )
    }
    single<MoviesLocalDataSource> { createMoviesLocalDataSource(androidContext()) }
    single<AppClock> { SystemAppClock }
    single<MoviesRepository> {
        DefaultMoviesRepository(
            remoteDataSource = get(),
            localDataSource = get(),
            clock = get(),
        )
    }
}

val appModules: List<Module> = listOf(applicationModule, trendingModule)
