package com.softwavegamess.smgrealestate.ui.listings

import com.softwavegamess.smgrealestate.domain.model.Property
import java.text.Normalizer
import java.util.Locale

internal fun List<Property>.matchingSearch(raw: String): List<Property> {
    val q = raw.trim().normalized()
    if (q.isEmpty()) return this
    return filter { it.normalizedLine().contains(q) }
}

private fun Property.normalizedLine(): String {
    val line = address?.toSingleLine().orEmpty()
    return "$title $line".normalized()
}

private fun String.normalized(): String {
    val stripped = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
    return stripped.lowercase(Locale.ROOT)
}
