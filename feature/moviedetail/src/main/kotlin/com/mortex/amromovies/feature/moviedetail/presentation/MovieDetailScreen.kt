package com.mortex.amromovies.feature.moviedetail.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import com.mortex.amromovies.feature.moviedetail.R
import com.mortex.amromovies.feature.moviedetail.presentation.model.MovieDetailUiModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    uiState: MovieDetailUiState,
    onAction: (MovieDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onOpenImdb: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.movie_detail_title)) },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text(stringResource(R.string.movie_detail_back))
                    }
                },
            )
        },
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        ) {
            when {
                uiState.isLoading -> LoadingState()
                uiState.error != null -> ErrorState(
                    error = uiState.error,
                    onRetry = { onAction(MovieDetailAction.Retry) },
                )
                uiState.movie != null -> MovieDetailsContent(
                    movie = uiState.movie,
                    onOpenImdb = onOpenImdb,
                )
                else -> EmptyState()
            }
        }
    }
}

@Composable
private fun MovieDetailsContent(
    movie: MovieDetailUiModel,
    onOpenImdb: (String) -> Unit,
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "backdrop") {
            DetailImage(
                url = movie.backdropUrl ?: movie.posterUrl,
                description = stringResource(R.string.movie_detail_backdrop, movie.title),
                unavailableDescription = stringResource(
                    R.string.movie_detail_image_unavailable,
                    movie.title,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            )
        }
        item(key = "content") {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    if (movie.posterUrl != null) {
                        DetailImage(
                            url = movie.posterUrl,
                            description = stringResource(R.string.movie_detail_poster, movie.title),
                            unavailableDescription = stringResource(
                                R.string.movie_detail_image_unavailable,
                                movie.title,
                            ),
                            modifier = Modifier
                                .size(width = 112.dp, height = 168.dp)
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(
                            text = movie.tagline
                                ?: stringResource(R.string.movie_detail_no_tagline),
                            style = MaterialTheme.typography.bodyLarge,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = movie.genres.joinToString()
                                .ifEmpty { stringResource(R.string.movie_detail_not_available) },
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                }

                DetailSection(
                    title = stringResource(R.string.movie_detail_overview),
                    value = movie.overview.ifEmpty {
                        stringResource(R.string.movie_detail_no_overview)
                    },
                )

                DetailGrid(movie)

                if (movie.imdbUrl != null) {
                    Button(
                        onClick = { onOpenImdb(movie.imdbUrl) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.movie_detail_imdb))
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailGrid(movie: MovieDetailUiModel) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DetailRow(
            label = stringResource(R.string.movie_detail_rating),
            value = stringResource(R.string.movie_detail_rating_value, movie.voteAverage),
        )
        DetailRow(
            label = stringResource(R.string.movie_detail_votes),
            value = movie.voteCount.toString(),
        )
        DetailRow(
            label = stringResource(R.string.movie_detail_runtime),
            value = movie.runtime ?: stringResource(R.string.movie_detail_not_available),
        )
        DetailRow(
            label = stringResource(R.string.movie_detail_release_date),
            value = movie.releaseDate ?: stringResource(R.string.movie_detail_not_available),
        )
        DetailRow(
            label = stringResource(R.string.movie_detail_status),
            value = movie.status.ifEmpty { stringResource(R.string.movie_detail_not_available) },
        )
        DetailRow(
            label = stringResource(R.string.movie_detail_budget),
            value = movie.budget ?: stringResource(R.string.movie_detail_not_available),
        )
        DetailRow(
            label = stringResource(R.string.movie_detail_revenue),
            value = movie.revenue ?: stringResource(R.string.movie_detail_not_available),
        )
        DetailRow(
            label = stringResource(R.string.movie_detail_genres),
            value = movie.genres.joinToString()
                .ifEmpty { stringResource(R.string.movie_detail_not_available) },
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = value,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DetailSection(title: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleMedium)
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DetailImage(
    url: String?,
    description: String,
    unavailableDescription: String,
    modifier: Modifier,
) {
    if (url == null) {
        ImageUnavailable(description = unavailableDescription, modifier = modifier)
        return
    }

    SubcomposeAsyncImage(
        model = url,
        contentDescription = description,
        modifier = modifier,
        contentScale = ContentScale.Crop,
        loading = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .semantics { contentDescription = description },
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.size(36.dp))
            }
        },
        error = {
            ImageUnavailable(
                description = unavailableDescription,
                modifier = Modifier.fillMaxSize(),
            )
        },
        success = { SubcomposeAsyncImageContent() },
    )
}

@Composable
private fun ImageUnavailable(description: String, modifier: Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = description,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LoadingState() {
    val description = stringResource(R.string.movie_detail_loading)
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
private fun ErrorState(error: MovieDetailUiError, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(text = error.message(), style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.movie_detail_retry)) }
    }
}

@Composable
private fun EmptyState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(R.string.movie_detail_no_data))
    }
}

@Composable
private fun MovieDetailUiError.message(): String = stringResource(
    when (this) {
        MovieDetailUiError.NO_INTERNET -> R.string.movie_detail_error_no_internet
        MovieDetailUiError.UNAUTHORIZED -> R.string.movie_detail_error_unauthorized
        MovieDetailUiError.NOT_FOUND -> R.string.movie_detail_error_not_found
        MovieDetailUiError.SERVER -> R.string.movie_detail_error_server
        MovieDetailUiError.UNKNOWN -> R.string.movie_detail_error_unknown
    },
)
