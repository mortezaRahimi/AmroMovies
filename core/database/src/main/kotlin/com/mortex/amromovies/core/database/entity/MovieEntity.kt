package com.mortex.amromovies.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
internal data class MovieEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val originalTitle: String,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
    val releaseDateEpochDay: Long?,
    val originalLanguage: String,
    val popularity: Double,
    val rating: Double,
    val voteCount: Int,
    val isAdult: Boolean,
)
