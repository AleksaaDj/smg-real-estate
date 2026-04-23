package com.softwavegamess.smgrealestate.domain.usecase

import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.domain.repository.PropertyRepository
import javax.inject.Inject

class GetPropertiesUseCase @Inject constructor(
    private val propertyRepository: PropertyRepository,
) {
    suspend operator fun invoke(): Result<List<Property>> = propertyRepository.getProperties()
}
