package com.softwavegamess.smgrealestate.domain.model

data class Address(
    val street: String?,
    val postalCode: String?,
    val locality: String?,
) {
    fun toSingleLine(): String {
        val postalLocality = listOfNotNull(
            postalCode?.trim()?.takeIf { it.isNotEmpty() },
            locality?.trim()?.takeIf { it.isNotEmpty() },
        ).joinToString(" ")

        val streetPart = street?.trim()?.takeIf { it.isNotEmpty() }

        return when {
            streetPart != null && postalLocality.isNotEmpty() -> "$streetPart, $postalLocality"
            streetPart != null -> streetPart
            postalLocality.isNotEmpty() -> postalLocality
            else -> ""
        }
    }
}
