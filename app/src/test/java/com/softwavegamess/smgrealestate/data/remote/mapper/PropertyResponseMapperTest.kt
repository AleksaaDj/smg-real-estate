package com.softwavegamess.smgrealestate.data.remote.mapper

import com.softwavegamess.smgrealestate.data.remote.dto.AddressDto
import com.softwavegamess.smgrealestate.data.remote.dto.BuyDto
import com.softwavegamess.smgrealestate.data.remote.dto.ListingDto
import com.softwavegamess.smgrealestate.data.remote.dto.ListingTypeWrapperDto
import com.softwavegamess.smgrealestate.data.remote.dto.ListerBrandingDto
import com.softwavegamess.smgrealestate.data.remote.dto.PricesDto
import com.softwavegamess.smgrealestate.data.remote.dto.PropertyResultDto
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PropertyResponseMapperTest {

    private val mapper = PropertyResponseMapper()

    @Test
    fun picksFirstImageAttachmentAndSkipsDocuments() {
        val localization = Json.parseToJsonElement(
            """
            {
              "primary": "de",
              "de": {
                "text": { "title": "Title" },
                "attachments": [
                  { "type": "DOCUMENT", "url": "https://example.com/doc.pdf" },
                  { "type": "IMAGE", "url": "https://example.com/first.jpg" },
                  { "type": "IMAGE", "url": "https://example.com/second.jpg" }
                ]
              }
            }
            """.trimIndent(),
        )

        val dto = PropertyResultDto(
            id = "1",
            listingType = ListingTypeWrapperDto(type = "TOP"),
            listing = ListingDto(
                id = "1",
                prices = PricesDto(currency = "CHF", buy = BuyDto(price = 10L)),
                address = AddressDto(street = "A", postalCode = "1", locality = "B"),
                localization = localization,
            ),
        )

        val property = mapper.map(dto)
        assertNotNull(property)
        assertEquals("https://example.com/first.jpg", property!!.imageUrl)
    }

    @Test
    fun fallsBackToListerBrandingLogoWhenNoImages() {
        val localization = Json.parseToJsonElement(
            """
            {
              "primary": "de",
              "de": {
                "text": { "title": "Only docs" },
                "attachments": [
                  { "type": "DOCUMENT", "url": "https://example.com/doc.pdf" }
                ]
              }
            }
            """.trimIndent(),
        )

        val dto = PropertyResultDto(
            id = "2",
            listingType = ListingTypeWrapperDto(type = "BASIC"),
            listerBranding = ListerBrandingDto(logoUrl = "https://example.com/logo.png"),
            listing = ListingDto(
                id = "2",
                prices = PricesDto(currency = "CHF", buy = BuyDto(price = 1L)),
                address = AddressDto(street = null, postalCode = "8000", locality = "Zürich"),
                localization = localization,
            ),
        )

        val property = mapper.map(dto)
        assertNotNull(property)
        assertEquals("https://example.com/logo.png", property!!.imageUrl)
    }

    @Test
    fun joinsPartialAddress() {
        val localization = Json.parseToJsonElement(
            """{"primary":"de","de":{"text":{"title":"T"},"attachments":[]}}""",
        )

        val dto = PropertyResultDto(
            id = "3",
            listing = ListingDto(
                id = "3",
                prices = PricesDto(currency = "CHF", buy = BuyDto(price = 5L)),
                address = AddressDto(street = null, postalCode = "8000", locality = "Zürich"),
                localization = localization,
            ),
        )

        val property = mapper.map(dto)
        assertNotNull(property)
        assertEquals("8000 Zürich", property!!.address!!.toSingleLine())
    }

    @Test
    fun mapsListingTypeCaseInsensitive() {
        val localization = Json.parseToJsonElement(
            """{"primary":"de","de":{"text":{"title":"T"},"attachments":[]}}""",
        )

        val dto = PropertyResultDto(
            id = "4",
            listingType = ListingTypeWrapperDto(type = "standard"),
            listing = ListingDto(
                id = "4",
                prices = PricesDto(currency = "CHF", buy = BuyDto(price = 1L)),
                address = AddressDto(),
                localization = localization,
            ),
        )

        val property = mapper.map(dto)
        assertNotNull(property)
        assertEquals(ListingTier.STANDARD, property!!.listingType)
    }

    @Test
    fun returnsNullWhenListingMissing() {
        val dto = PropertyResultDto(id = "5", listing = null)
        assertNull(mapper.map(dto))
    }
}
