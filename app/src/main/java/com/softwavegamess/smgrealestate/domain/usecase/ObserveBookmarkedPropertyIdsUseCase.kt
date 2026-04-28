package com.softwavegamess.smgrealestate.domain.usecase

import com.softwavegamess.smgrealestate.domain.repository.PropertyRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class ObserveBookmarkedPropertyIdsUseCase @Inject constructor(
    private val propertyRepository: PropertyRepository,
) {
    operator fun invoke(): Flow<Set<String>> = propertyRepository.observeBookmarkedPropertyIds()
}

