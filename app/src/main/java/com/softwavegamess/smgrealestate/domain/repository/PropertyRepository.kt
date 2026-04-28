package com.softwavegamess.smgrealestate.domain.repository

import com.softwavegamess.smgrealestate.domain.model.Property
import kotlinx.coroutines.flow.Flow

interface PropertyRepository {
    suspend fun getProperties(): Result<List<Property>>

    suspend fun toggleBookmark(propertyId: String): Result<Boolean>

    fun observeBookmarkedPropertyIds(): Flow<Set<String>>
}
