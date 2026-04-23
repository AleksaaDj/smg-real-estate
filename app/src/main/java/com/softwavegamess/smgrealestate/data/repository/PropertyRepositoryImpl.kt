package com.softwavegamess.smgrealestate.data.repository

import com.softwavegamess.smgrealestate.data.remote.ApiService
import com.softwavegamess.smgrealestate.data.remote.mapper.PropertyResponseMapper
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.domain.repository.PropertyRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PropertyRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val mapper: PropertyResponseMapper,
) : PropertyRepository {

    override suspend fun getProperties(): Result<List<Property>> = withContext(Dispatchers.IO) {
        runCatching {
            val body = api.getProperties()
            body.results
                .asSequence()
                .mapNotNull { mapper.map(it) }
                .sortedWith(
                    compareBy<Property> { it.listingType.toSortRank() }
                        .thenBy { it.id },
                )
                .toList()
        }
    }
}

private fun ListingTier.toSortRank(): Int = when (this) {
    ListingTier.TOP -> 0
    ListingTier.STANDARD -> 1
    ListingTier.BASIC -> 2
    ListingTier.UNKNOWN -> 3
}
