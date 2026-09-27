package com.mortex.amromovies.feature.trending.presentation

import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieSort
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import com.mortex.amromovies.feature.trending.presentation.model.SortDirection
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.util.Locale

internal fun filterAndSortMovies(
    movies: List<MovieUiModel>,
    selectedGenreId: Int?,
    sort: MovieSort,
    direction: SortDirection,
): ImmutableList<MovieUiModel> {
    val filtered = movies
        .take(MAX_TRENDING_MOVIES)
        .filter { movie ->
            selectedGenreId == null || movie.genres.any { genre -> genre.id == selectedGenreId }
        }

    return when (sort) {
        MovieSort.POPULARITY -> filtered.sortedWith(
            direction.applyTo(
                compareBy<MovieUiModel>(MovieUiModel::popularity)
                    .thenBy(String.CASE_INSENSITIVE_ORDER) { movie -> movie.title }
                    .thenBy(MovieUiModel::id),
            ),
        )

        MovieSort.TITLE -> filtered.sortedWith(
            direction.applyTo(
                compareBy<MovieUiModel> { movie -> movie.title.lowercase(Locale.ROOT) }
                    .thenBy(MovieUiModel::id),
            ),
        )

        MovieSort.RELEASE_DATE -> {
            val (dated, missingDates) = filtered.partition { movie -> movie.releaseDate != null }
            val dateComparator = compareBy<MovieUiModel> { movie -> movie.releaseDate }
                .thenBy(String.CASE_INSENSITIVE_ORDER) { movie -> movie.title }
                .thenBy(MovieUiModel::id)
            val missingDateComparator = compareBy<MovieUiModel> { movie ->
                movie.title.lowercase(Locale.ROOT)
            }.thenBy(MovieUiModel::id)

            dated.sortedWith(direction.applyTo(dateComparator)) +
                missingDates.sortedWith(missingDateComparator)
        }
    }.toPersistentList()
}

internal fun availableGenres(movies: List<MovieUiModel>): ImmutableList<GenreUiModel> = movies
    .take(MAX_TRENDING_MOVIES)
    .flatMap(MovieUiModel::genres)
    .distinctBy(GenreUiModel::id)
    .sortedWith(
        compareBy<GenreUiModel> { genre -> genre.name.lowercase(Locale.ROOT) }
            .thenBy(GenreUiModel::id),
    )
    .toPersistentList()

private fun <T> SortDirection.applyTo(comparator: Comparator<T>): Comparator<T> = when (this) {
    SortDirection.ASCENDING -> comparator
    SortDirection.DESCENDING -> comparator.reversed()
}

private const val MAX_TRENDING_MOVIES = 100
