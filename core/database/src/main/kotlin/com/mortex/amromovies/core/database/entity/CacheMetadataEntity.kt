package com.mortex.amromovies.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cache_metadata")
internal data class CacheMetadataEntity(
    @PrimaryKey val key: String,
    val updatedAtEpochMillis: Long,
)
