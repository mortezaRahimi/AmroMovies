package com.mortex.amromovies.di

import com.mortex.amromovies.core.data.source.MoviesLocalDataSource
import com.mortex.amromovies.core.database.createMoviesLocalDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

internal val databaseModule = module {
    single<MoviesLocalDataSource> {
        createMoviesLocalDataSource(context = androidContext())
    }
}
