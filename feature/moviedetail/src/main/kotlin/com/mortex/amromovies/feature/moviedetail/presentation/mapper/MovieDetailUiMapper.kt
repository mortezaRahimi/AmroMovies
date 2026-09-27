package com.mortex.amromovies.feature.moviedetail.presentation.mapper

import com.mortex.amromovies.core.domain.model.MovieDetails
import com.mortex.amromovies.feature.moviedetail.presentation.model.MovieDetailUiModel
import kotlinx.collections.immutable.toPersistentList
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Currency
import java.util.Locale

internal fun MovieDetails.toUiModel(locale: Locale = Locale.getDefault()): MovieDetailUiModel {
    val currencyFormatter = NumberFormat.getCurrencyInstance(locale).apply {
        currency = Currency.getInstance(USD_CURRENCY_CODE)
        maximumFractionDigits = 0
    }
    val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale)

    return MovieDetailUiModel(
        id = id,
        title = title,
        posterUrl = posterPath.toImageUrl(POSTER_SIZE),
        backdropUrl = backdropPath.toImageUrl(BACKDROP_SIZE),
        tagline = tagline.trim().takeIf(String::isNotEmpty),
        overview = overview.trim(),
        voteAverage = rating,
        voteCount = voteCount,
        budget = budget.takeIf { value -> value > 0 }?.let(currencyFormatter::format),
        revenue = revenue.takeIf { value -> value > 0 }?.let(currencyFormatter::format),
        status = status.trim(),
        imdbUrl = imdbId
            ?.trim()
            ?.takeIf(IMDB_ID_PATTERN::matches)
            ?.let { id -> "$IMDB_TITLE_BASE_URL$id" },
        runtime = runtimeMinutes.toRuntimeDisplay(),
        releaseDate = releaseDate?.format(dateFormatter),
        genres = genres.map { genre -> genre.name }.toPersistentList(),
    )
}

private fun String?.toImageUrl(size: String): String? = this
    ?.trim()
    ?.takeIf(String::isNotEmpty)
    ?.let { path -> "$TMDB_IMAGE_BASE_URL$size$path" }

private fun Int?.toRuntimeDisplay(): String? {
    val totalMinutes = this?.takeIf { minutes -> minutes > 0 } ?: return null
    val hours = totalMinutes / MINUTES_PER_HOUR
    val minutes = totalMinutes % MINUTES_PER_HOUR
    return when {
        hours == 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h ${minutes}m"
    }
}

private val IMDB_ID_PATTERN = Regex("tt\\d+")
private const val TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
private const val POSTER_SIZE = "w500"
private const val BACKDROP_SIZE = "w780"
private const val IMDB_TITLE_BASE_URL = "https://www.imdb.com/title/"
private const val USD_CURRENCY_CODE = "USD"
private const val MINUTES_PER_HOUR = 60
