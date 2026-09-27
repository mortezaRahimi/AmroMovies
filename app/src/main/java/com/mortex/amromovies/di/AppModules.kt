package com.mortex.amromovies.di

import com.mortex.amromovies.BuildConfig
import com.mortex.amromovies.core.network.TmdbNetworkClient
import org.koin.core.module.Module
import org.koin.dsl.module

private val applicationModule = module {
    single {
        TmdbNetworkClient(
            accessToken = BuildConfig.TMDB_ACCESS_TOKEN,
            enableHttpLogging = BuildConfig.DEBUG,
        )
    }
}

val appModules: List<Module> = listOf(applicationModule)
