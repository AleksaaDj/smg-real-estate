package com.softwavegamess.smgrealestate.data.remote.mapper

import com.softwavegamess.smgrealestate.data.remote.dto.AddressDto
import com.softwavegamess.smgrealestate.data.remote.dto.ListingDto
import com.softwavegamess.smgrealestate.data.remote.dto.PricesDto
import com.softwavegamess.smgrealestate.data.remote.dto.PropertyResultDto
import com.softwavegamess.smgrealestate.domain.model.Address
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Price
import com.softwavegamess.smgrealestate.domain.model.Property
import javax.inject.Inject
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PropertyResponseMapper @Inject constructor() {

    fun map(result: PropertyResultDto): Property? {
        val listing = result.listing ?: return null
        val id = listing.id?.takeIf { it.isNotBlank() }
            ?: result.id?.takeIf { it.isNotBlank() }
            ?: return null

        val listingType = result.listingType?.type.toListingTier()
        val imageUrl = firstImageUrl(listing.localization)
            ?: result.listerBranding?.logoUrl?.takeIf { it.isNotBlank() }

        return Property(
            id = id,
            title = resolveTitle(listing),
            imageUrl = imageUrl?.takeIf { it.isNotBlank() },
            price = resolvePrice(listing.prices),
            address = listing.address.toDomain(),
            listingType = listingType,
        )
    }

    private fun resolveTitle(listing: ListingDto): String {
        val localized = titleFromLocalization(listing.localization)
        if (!localized.isNullOrBlank()) return localized.trim()
        val fromCategories = listing.categories
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .joinToString(" · ")
        if (fromCategories.isNotBlank()) return fromCategories
        return "Untitled"
    }

    private fun titleFromLocalization(localization: JsonElement?): String? {
        val lang = resolveLocalizationObject(localization) ?: return null
        return lang["text"]
            ?.jsonObject
            ?.get("title")
            ?.jsonPrimitive
            ?.contentOrNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }
    }

    private fun resolveLocalizationObject(localization: JsonElement?): JsonObject? {
        val root = localization?.jsonObject ?: return null
        val primaryKey = root["primary"]?.jsonPrimitive?.contentOrNull
        if (!primaryKey.isNullOrBlank()) {
            when (val node = root[primaryKey]) {
                is JsonObject -> return node
                else -> Unit
            }
        }
        for ((key, value) in root) {
            if (key == "primary") continue
            if (value is JsonObject) return value
        }
        return null
    }

    private fun firstImageUrl(localization: JsonElement?): String? {
        val lang = resolveLocalizationObject(localization) ?: return null
        val attachments = lang["attachments"] as? JsonArray ?: return null
        for (element in attachments) {
            val obj = element as? JsonObject ?: continue
            val type = obj["type"]?.jsonPrimitive?.contentOrNull
            if (!type.equals("IMAGE", ignoreCase = true)) continue
            val url = obj["url"]?.jsonPrimitive?.contentOrNull?.trim()
            if (!url.isNullOrEmpty()) return url
        }
        return null
    }

    private fun resolvePrice(prices: PricesDto?): Price? {
        if (prices == null) return null
        val amount = prices.buy?.price ?: return null
        val currency = prices.currency?.trim()?.takeIf { it.isNotEmpty() } ?: "CHF"
        return Price(amount = amount, currencyCode = currency)
    }

    private fun AddressDto?.toDomain(): Address? {
        if (this == null) return null
        if (street.isNullOrBlank() && postalCode.isNullOrBlank() && locality.isNullOrBlank()) return null
        return Address(
            street = street?.takeIf { it.isNotBlank() },
            postalCode = postalCode?.takeIf { it.isNotBlank() },
            locality = locality?.takeIf { it.isNotBlank() },
        )
    }

    private fun String?.toListingTier(): ListingTier = when (this?.trim()?.uppercase()) {
        "TOP" -> ListingTier.TOP
        "STANDARD" -> ListingTier.STANDARD
        "BASIC" -> ListingTier.BASIC
        else -> ListingTier.UNKNOWN
    }
}
