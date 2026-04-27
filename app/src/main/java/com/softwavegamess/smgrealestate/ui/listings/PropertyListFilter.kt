package com.softwavegamess.smgrealestate.ui.listings

import com.softwavegamess.smgrealestate.domain.model.Property

internal fun List<Property>.matchingSearch(raw: String): List<Property> {
    val q = raw.trim().collateNormalized()
    if (q.isEmpty()) return this
    return filter { it.normalizedLine().contains(q) }
}

private fun Property.normalizedLine(): String {
    val line = address?.toSingleLine().orEmpty()
    return "$title $line".collateNormalized()
}
