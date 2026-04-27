package com.softwavegamess.smgrealestate.ui.listings

import com.softwavegamess.smgrealestate.domain.model.Address
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Price
import com.softwavegamess.smgrealestate.domain.model.Property
import org.junit.Assert.assertEquals
import org.junit.Test

class PropertyListSortTest {

    private fun p(id: String, title: String, amount: Long?, tier: ListingTier = ListingTier.STANDARD) = Property(
        id = id,
        title = title,
        imageUrl = null,
        price = if (amount != null) Price(amount, "CHF") else Price(null, "CHF"),
        address = Address(null, null, null),
        listingType = tier,
        isBookmarked = false,
    )

    @Test
    fun defaultLeavesOrder() {
        val list = listOf(p("2", "B", 200L), p("1", "A", 100L))
        assertEquals(list, list.sortedByOption(ListingSortOption.DEFAULT))
    }

    @Test
    fun priceAscendingNullsLast() {
        val list = listOf(
            p("1", "A", 300L),
            p("2", "B", null),
            p("3", "C", 100L),
        )
        val sorted = list.sortedByOption(ListingSortOption.PRICE_ASC)
        assertEquals(listOf(p("3", "C", 100L), p("1", "A", 300L), p("2", "B", null)), sorted)
    }

    @Test
    fun priceDescendingNullsLast() {
        val list = listOf(
            p("1", "A", 100L),
            p("2", "B", null),
            p("3", "C", 500L),
        )
        val sorted = list.sortedByOption(ListingSortOption.PRICE_DESC)
        assertEquals(listOf(p("3", "C", 500L), p("1", "A", 100L), p("2", "B", null)), sorted)
    }

    @Test
    fun titleSortCaseAndAccentInsensitive() {
        val list = listOf(
            p("1", "Zebra", 1L),
            p("2", "Áloe", 1L),
            p("3", "beta", 1L),
        )
        val sorted = list.sortedByOption(ListingSortOption.TITLE_A_Z)
        assertEquals(listOf("2", "3", "1"), sorted.map { it.id })
    }

    @Test
    fun stableTieOnPriceUsesId() {
        val list = listOf(p("z", "A", 100L), p("a", "B", 100L))
        val sorted = list.sortedByOption(ListingSortOption.PRICE_ASC)
        assertEquals(listOf(p("a", "B", 100L), p("z", "A", 100L)), sorted)
    }
}
