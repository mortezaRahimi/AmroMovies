package com.mortex.amromovies.core.database.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

internal data class TrendingMovieWithGenres(
    @Embedded val movie: MovieEntity,
    val trendingRank: Int,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MovieGenreCrossRef::class,
            parentColumn = "movieId",
            entityColumn = "genreId",
        ),
    )
    val genres: List<GenreEntity>,
)
