package com.softwavegamess.smgrealestate.data.remote.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class PropertiesResponseDto(
    val results: List<PropertyResultDto> = emptyList(),
)

@Serializable
data class PropertyResultDto(
    val id: String? = null,
    val listingType: ListingTypeWrapperDto? = null,
    val listerBranding: ListerBrandingDto? = null,
    val listing: ListingDto? = null,
)

@Serializable
data class ListingTypeWrapperDto(
    val type: String? = null,
)

@Serializable
data class ListerBrandingDto(
    val logoUrl: String? = null,
)

@Serializable
data class ListingDto(
    val id: String? = null,
    val categories: List<String> = emptyList(),
    val prices: PricesDto? = null,
    val address: AddressDto? = null,
    val localization: JsonElement? = null,
)

@Serializable
data class PricesDto(
    val currency: String? = null,
    val buy: BuyDto? = null,
)

@Serializable
data class BuyDto(
    val price: Long? = null,
)

@Serializable
data class AddressDto(
    val street: String? = null,
    val postalCode: String? = null,
    val locality: String? = null,
)
