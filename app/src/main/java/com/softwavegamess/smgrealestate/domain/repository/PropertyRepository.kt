package com.softwavegamess.smgrealestate.domain.repository

import com.softwavegamess.smgrealestate.domain.model.Property

interface PropertyRepository {
    suspend fun getProperties(): Result<List<Property>>

    suspend fun toggleBookmark(propertyId: String): Result<Boolean>
}
