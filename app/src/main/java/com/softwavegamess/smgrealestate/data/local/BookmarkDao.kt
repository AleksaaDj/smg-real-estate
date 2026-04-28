package com.softwavegamess.smgrealestate.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT propertyId FROM bookmarks")
    suspend fun getBookmarkedPropertyIds(): List<String>

    @Query("SELECT propertyId FROM bookmarks")
    fun observeBookmarkedPropertyIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE propertyId = :propertyId LIMIT 1)")
    suspend fun isBookmarked(propertyId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE propertyId = :propertyId")
    suspend fun deleteByPropertyId(propertyId: String)
}
