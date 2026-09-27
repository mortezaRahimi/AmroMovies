package com.mortex.amromovies.di

import com.mortex.amromovies.core.common.time.AppClock
import com.mortex.amromovies.core.common.time.SystemAppClock
import com.mortex.amromovies.core.data.DefaultMoviesRepository
import com.mortex.amromovies.core.domain.repository.MoviesRepository
import org.koin.dsl.module

internal val repositoryModule = module {
    single<AppClock> { SystemAppClock }
    single<MoviesRepository> {
        DefaultMoviesRepository(
            remoteDataSource = get(),
            localDataSource = get(),
            clock = get(),
        )
    }
}
