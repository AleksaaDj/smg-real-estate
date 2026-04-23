package com.softwavegamess.smgrealestate.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey val propertyId: String,
    val createdAtMillis: Long,
)
