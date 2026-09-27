package com.mortex.amromovies.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genres")
internal data class GenreEntity(
    @PrimaryKey val id: Int,
    val name: String,
)
