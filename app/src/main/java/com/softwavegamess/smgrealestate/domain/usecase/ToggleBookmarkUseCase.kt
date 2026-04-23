package com.softwavegamess.smgrealestate.domain.usecase

import com.softwavegamess.smgrealestate.domain.repository.PropertyRepository
import javax.inject.Inject

class ToggleBookmarkUseCase @Inject constructor(
    private val propertyRepository: PropertyRepository,
) {
    suspend operator fun invoke(propertyId: String): Result<Boolean> =
        propertyRepository.toggleBookmark(propertyId)
}
