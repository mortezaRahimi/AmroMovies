package com.mortex.amromovies.di

import com.mortex.amromovies.BuildConfig
import com.mortex.amromovies.core.data.source.MoviesRemoteDataSource
import com.mortex.amromovies.core.network.TmdbNetworkClient
import org.koin.dsl.module

internal val networkModule = module {
    single<MoviesRemoteDataSource> {
        TmdbNetworkClient(
            accessToken = BuildConfig.TMDB_ACCESS_TOKEN,
            enableHttpLogging = BuildConfig.DEBUG,
        )
    }
}
