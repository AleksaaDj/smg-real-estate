package com.softwavegamess.smgrealestate.ui.listings

import java.text.Normalizer
import java.util.Locale

internal fun String.collateNormalized(): String {
    val stripped = Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
    return stripped.lowercase(Locale.ROOT)
}
