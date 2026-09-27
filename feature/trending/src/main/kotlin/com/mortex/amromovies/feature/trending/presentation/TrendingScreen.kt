package com.mortex.amromovies.feature.trending.presentation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
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
            FilterAndSortControls(
                genres = uiState.genres,
                selectedGenreId = uiState.selectedGenreId,
                selectedSort = uiState.sortOption,
                direction = uiState.sortDirection,
                onGenreSelected = { genreId ->
                    onAction(TrendingAction.SelectGenre(genreId))
                },
                onSortSelected = { sort -> onAction(TrendingAction.SelectSort(sort)) },
                onDirectionSelected = { direction ->
                    onAction(TrendingAction.SelectSortDirection(direction))
                },
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
private fun FilterAndSortControls(
    genres: List<GenreUiModel>,
    selectedGenreId: Int?,
    selectedSort: MovieSort,
    direction: SortDirection,
    onGenreSelected: (Int?) -> Unit,
    onSortSelected: (MovieSort) -> Unit,
    onDirectionSelected: (SortDirection) -> Unit,
) {
    var openSheet by rememberSaveable { mutableStateOf<ControlSheet?>(null) }
    val selectedGenre = genres.firstOrNull { genre -> genre.id == selectedGenreId }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SelectionControl(
            label = stringResource(R.string.trending_genre_control),
            value = selectedGenre?.name ?: stringResource(R.string.trending_all_genres),
            onClick = { openSheet = ControlSheet.GENRE },
            modifier = Modifier.weight(1f),
        )
        SelectionControl(
            label = stringResource(R.string.trending_sort_control),
            value = "${selectedSort.label()} · ${direction.label()}",
            onClick = { openSheet = ControlSheet.SORT },
            modifier = Modifier.weight(1f),
        )
    }

    when (openSheet) {
        ControlSheet.GENRE -> GenrePickerSheet(
            genres = genres,
            selectedGenreId = selectedGenreId,
            onGenreSelected = { genreId ->
                onGenreSelected(genreId)
                openSheet = null
            },
            onDismiss = { openSheet = null },
        )
        ControlSheet.SORT -> SortPickerSheet(
            selectedSort = selectedSort,
            direction = direction,
            onSortSelected = onSortSelected,
            onDirectionSelected = onDirectionSelected,
            onDismiss = { openSheet = null },
        )
        null -> Unit
    }
}

@Composable
private fun SelectionControl(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = stringResource(R.string.trending_selection_control_description, label, value)
    Surface(
        onClick = onClick,
        modifier = modifier
            .height(64.dp)
            .clearAndSetSemantics {
                contentDescription = description
                role = Role.Button
                this.onClick {
                    onClick()
                    true
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GenrePickerSheet(
    genres: List<GenreUiModel>,
    selectedGenreId: Int?,
    onGenreSelected: (Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.trending_choose_genre),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item(key = "all_genres") {
                RadioOption(
                    label = stringResource(R.string.trending_all_genres),
                    selected = selectedGenreId == null,
                    onClick = { onGenreSelected(null) },
                )
            }
            items(items = genres, key = { genre -> genre.id }) { genre ->
                RadioOption(
                    label = genre.name,
                    selected = selectedGenreId == genre.id,
                    onClick = { onGenreSelected(genre.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortPickerSheet(
    selectedSort: MovieSort,
    direction: SortDirection,
    onSortSelected: (MovieSort) -> Unit,
    onDirectionSelected: (SortDirection) -> Unit,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Text(
            text = stringResource(R.string.trending_choose_sort),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        MovieSort.entries.forEach { sort ->
            RadioOption(
                label = sort.label(),
                selected = selectedSort == sort,
                onClick = { onSortSelected(sort) },
            )
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = stringResource(R.string.trending_direction_label),
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        SortDirection.entries.forEach { option ->
            RadioOption(
                label = option.label(),
                selected = direction == option,
                onClick = { onDirectionSelected(option) },
            )
        }
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .align(Alignment.End)
                .padding(24.dp),
        ) {
            Text(stringResource(R.string.trending_done))
        }
    }
}

@Composable
private fun RadioOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .selectable(selected = selected, onClick = onClick, role = Role.RadioButton)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(selected = selected, onClick = null)
        Spacer(Modifier.width(16.dp))
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
    }
}

private enum class ControlSheet {
    GENRE,
    SORT,
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
        contentPadding = PaddingValues(top =16.dp , start = 16.dp , end = 16.dp),
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
