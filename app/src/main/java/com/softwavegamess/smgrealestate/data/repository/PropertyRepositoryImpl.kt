package com.softwavegamess.smgrealestate.data.repository

import com.softwavegamess.smgrealestate.data.local.BookmarkDao
import com.softwavegamess.smgrealestate.data.local.BookmarkEntity
import com.softwavegamess.smgrealestate.data.remote.ApiService
import com.softwavegamess.smgrealestate.data.remote.mapper.PropertyResponseMapper
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.domain.repository.PropertyRepository
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class PropertyRepositoryImpl @Inject constructor(
    private val api: ApiService,
    private val mapper: PropertyResponseMapper,
    private val bookmarkDao: BookmarkDao,
) : PropertyRepository {

    override suspend fun getProperties(): Result<List<Property>> = withContext(Dispatchers.IO) {
        runCatching {
            val body = api.getProperties()
            val bookmarkIds = bookmarkDao.getBookmarkedPropertyIds().toSet()
            body.results
                .asSequence()
                .mapNotNull { mapper.map(it) }
                .sortedWith(
                    compareBy<Property> { it.listingType.toSortRank() }
                        .thenBy { it.title.lowercase(Locale.ROOT) }
                        .thenBy { it.id },
                )
                .map { property ->
                    property.copy(isBookmarked = bookmarkIds.contains(property.id))
                }
                .toList()
        }
    }

    override suspend fun toggleBookmark(propertyId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        runCatching {
            if (bookmarkDao.isBookmarked(propertyId)) {
                bookmarkDao.deleteByPropertyId(propertyId)
                false
            } else {
                bookmarkDao.insert(
                    BookmarkEntity(
                        propertyId = propertyId,
                        createdAtMillis = System.currentTimeMillis(),
                    ),
                )
                bookmarkDao.isBookmarked(propertyId)
            }
        }
    }
}

private fun ListingTier.toSortRank(): Int = when (this) {
    ListingTier.TOP -> 0
    ListingTier.STANDARD -> 1
    ListingTier.BASIC -> 2
    ListingTier.UNKNOWN -> 3
}
