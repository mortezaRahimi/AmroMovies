package com.mortex.amromovies.feature.trending.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.mortex.amromovies.feature.trending.R
import com.mortex.amromovies.feature.trending.presentation.model.GenreUiModel
import com.mortex.amromovies.feature.trending.presentation.model.MovieSort
import com.mortex.amromovies.feature.trending.presentation.model.MovieUiModel
import com.mortex.amromovies.feature.trending.presentation.model.SortDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(
    uiState: TrendingUiState,
    onAction: (TrendingAction) -> Unit,
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { TopAppBar(title = { Text(stringResource(R.string.trending_title)) }) },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            GenreFilters(
                genres = uiState.genres,
                selectedGenreId = uiState.selectedGenreId,
                onGenreSelected = { genreId ->
                    onAction(TrendingAction.SelectGenre(genreId))
                },
            )
            SortControls(
                selectedSort = uiState.sortOption,
                direction = uiState.sortDirection,
                onSortSelected = { sort -> onAction(TrendingAction.SelectSort(sort)) },
                onDirectionToggle = { onAction(TrendingAction.ToggleSortDirection) },
            )

            if (uiState.isRefreshing) {
                val description = stringResource(R.string.trending_refreshing)
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = description },
                )
            }

            TrendingContent(
                uiState = uiState,
                onRetry = { onAction(TrendingAction.Retry) },
                onMovieClick = onMovieClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GenreFilters(
    genres: List<GenreUiModel>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.trending_genre_filter_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedGenreId == null,
                onClick = { onGenreSelected(null) },
                label = { Text(stringResource(R.string.trending_all_genres)) },
            )
            genres.forEach { genre ->
                FilterChip(
                    selected = selectedGenreId == genre.id,
                    onClick = { onGenreSelected(genre.id) },
                    label = { Text(genre.name) },
                )
            }
        }
    }
}

@Composable
private fun SortControls(
    selectedSort: MovieSort,
    direction: SortDirection,
    onSortSelected: (MovieSort) -> Unit,
    onDirectionToggle: () -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(
            text = stringResource(R.string.trending_sort_label),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MovieSort.entries.forEach { sort ->
                FilterChip(
                    selected = selectedSort == sort,
                    onClick = { onSortSelected(sort) },
                    label = { Text(sort.label()) },
                )
            }
            Button(onClick = onDirectionToggle) {
                Text(direction.label())
            }
        }
    }
}

@Composable
private fun TrendingContent(
    uiState: TrendingUiState,
    onRetry: () -> Unit,
    onMovieClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxWidth()) {
        when {
            uiState.isLoading && uiState.movies.isEmpty() -> LoadingState()
            uiState.error != null && !uiState.hasUnfilteredMovies -> ErrorState(
                error = uiState.error,
                onRetry = onRetry,
            )
            !uiState.hasUnfilteredMovies -> EmptyState(hasSelectedGenre = false)
            uiState.movies.isEmpty() -> FilteredEmptyState(
                error = uiState.error,
                onRetry = onRetry,
            )
            else -> MovieList(
                movies = uiState.movies,
                error = uiState.error,
                onRetry = onRetry,
                onMovieClick = onMovieClick,
            )
        }
    }
}

@Composable
private fun FilteredEmptyState(error: TrendingUiError?, onRetry: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        if (error != null) {
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                InlineError(error = error, onRetry = onRetry)
            }
        }
        Box(modifier = Modifier.weight(1f)) {
            EmptyState(hasSelectedGenre = true)
        }
    }
}

@Composable
private fun MovieList(
    movies: List<MovieUiModel>,
    error: TrendingUiError?,
    onRetry: () -> Unit,
    onMovieClick: (Long) -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (error != null) {
            item(key = "refresh_error") {
                InlineError(error = error, onRetry = onRetry)
            }
        }
        items(items = movies, key = { movie -> movie.id }) { movie ->
            MovieCard(movie = movie, onClick = { onMovieClick(movie.id) })
        }
    }
}

@Composable
internal fun MovieCard(
    movie: MovieUiModel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            MoviePoster(movie = movie)
            Spacer(Modifier.width(16.dp))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = movie.genres.joinToString { genre -> genre.name }
                        .ifEmpty { stringResource(R.string.trending_genre_unavailable) },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(R.string.trending_rating, movie.rating),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
private fun MoviePoster(movie: MovieUiModel) {
    val modifier = Modifier
        .size(width = 104.dp, height = 156.dp)
        .clip(RoundedCornerShape(8.dp))
    val posterUrl = movie.posterUrl

    if (posterUrl == null) {
        PosterUnavailable(movie = movie, modifier = modifier)
        return
    }

    SubcomposeAsyncImage(
        model = posterUrl,
        contentDescription = stringResource(R.string.trending_movie_poster, movie.title),
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            val description = stringResource(R.string.trending_movie_poster_loading, movie.title)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(32.dp))
            }
        },
        error = { PosterUnavailable(movie = movie, modifier = Modifier.fillMaxSize()) },
        success = { SubcomposeAsyncImageContent() },
    )
}

@Composable
private fun PosterUnavailable(movie: MovieUiModel, modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.trending_movie_poster_unavailable, movie.title),
            modifier = Modifier.padding(8.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingState() {
    val description = stringResource(R.string.trending_loading)
    Box(
        modifier = Modifier
            .fillMaxSize()
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorState(error: TrendingUiError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = error.message(), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.trending_retry)) }
    }
}

@Composable
private fun InlineError(error: TrendingUiError, onRetry: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        shape = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = error.message(), modifier = Modifier.weight(1f))
            TextButton(onClick = onRetry) { Text(stringResource(R.string.trending_retry)) }
        }
    }
}

@Composable
private fun EmptyState(hasSelectedGenre: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = stringResource(
                if (hasSelectedGenre) R.string.trending_empty_genre else R.string.trending_empty,
            ),
            modifier = Modifier.padding(32.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun MovieSort.label(): String = stringResource(
    when (this) {
        MovieSort.POPULARITY -> R.string.trending_sort_popularity
        MovieSort.TITLE -> R.string.trending_sort_title
        MovieSort.RELEASE_DATE -> R.string.trending_sort_release_date
    },
)

@Composable
private fun SortDirection.label(): String = stringResource(
    when (this) {
        SortDirection.ASCENDING -> R.string.trending_sort_ascending
        SortDirection.DESCENDING -> R.string.trending_sort_descending
    },
)

@Composable
private fun TrendingUiError.message(): String = stringResource(
    when (this) {
        TrendingUiError.NO_INTERNET -> R.string.trending_error_no_internet
        TrendingUiError.UNAUTHORIZED -> R.string.trending_error_unauthorized
        TrendingUiError.SERVER -> R.string.trending_error_server
        TrendingUiError.NOT_FOUND -> R.string.trending_error_not_found
        TrendingUiError.UNKNOWN -> R.string.trending_error_unknown
    },
)
