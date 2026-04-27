package com.softwavegamess.smgrealestate.ui.listings

import com.softwavegamess.smgrealestate.domain.model.Property

internal fun List<Property>.sortedByOption(option: ListingSortOption): List<Property> = when (option) {
    ListingSortOption.DEFAULT -> this
    ListingSortOption.PRICE_ASC -> sortedWith(priceComparator(ascending = true))
    ListingSortOption.PRICE_DESC -> sortedWith(priceComparator(ascending = false))
    ListingSortOption.TITLE_A_Z -> sortedWith(titleComparator())
}

private fun Property.numericPriceOrNull(): Long? = price?.amount

/**
 * Null / missing numeric price sorts after any priced row (both directions).
 */
private fun priceComparator(ascending: Boolean): Comparator<Property> =
    Comparator { a, b ->
        val pa = a.numericPriceOrNull()
        val pb = b.numericPriceOrNull()
        when {
            pa == null && pb == null -> a.id.compareTo(b.id)
            pa == null -> 1
            pb == null -> -1
            pa == pb -> a.id.compareTo(b.id)
            else -> if (ascending) pa.compareTo(pb) else pb.compareTo(pa)
        }
    }

private fun titleComparator(): Comparator<Property> =
    compareBy<Property> { it.title.collateNormalized() }.thenBy { it.id }
