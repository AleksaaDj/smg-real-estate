package com.softwavegamess.smgrealestate.ui.listings

import com.softwavegamess.smgrealestate.domain.model.Address
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Price
import com.softwavegamess.smgrealestate.domain.model.Property
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PropertyListFilterTest {

    private fun p(id: String, title: String, street: String? = null, postal: String? = null, locality: String? = null) =
        Property(
            id = id,
            title = title,
            imageUrl = null,
            price = Price(1L, "CHF"),
            address = Address(street, postal, locality),
            listingType = ListingTier.STANDARD,
            isBookmarked = false,
        )

    @Test
    fun blankQueryReturnsAll() {
        val list = listOf(p("1", "A"), p("2", "B"))
        assertEquals(list, list.matchingSearch("   "))
    }

    @Test
    fun matchesTitleCaseInsensitive() {
        val list = listOf(p("1", "Lake House"), p("2", "City flat"))
        assertEquals(listOf(list[0]), list.matchingSearch("LAKE"))
    }

    @Test
    fun matchesAddressPostal() {
        val list = listOf(p("1", "A", locality = "Bern"), p("2", "B", postal = "8001", locality = "Zürich"))
        assertEquals(listOf(list[1]), list.matchingSearch("8001"))
    }

    @Test
    fun accentInsensitiveForGermanUmlaut() {
        val list = listOf(p("1", "Flat", locality = "Zürich"))
        assertEquals(listOf(list[0]), list.matchingSearch("zurich"))
        assertEquals(listOf(list[0]), list.matchingSearch("ZÜRICH"))
    }

    @Test
    fun noSubstringNoMatch() {
        val list = listOf(p("1", "Alpha"))
        assertTrue(list.matchingSearch("zzz").isEmpty())
    }
}
