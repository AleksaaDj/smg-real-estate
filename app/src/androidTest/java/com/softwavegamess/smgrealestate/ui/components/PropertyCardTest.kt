package com.softwavegamess.smgrealestate.ui.components

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.softwavegamess.smgrealestate.R
import com.softwavegamess.smgrealestate.domain.model.Address
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Price
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.ui.theme.SMGRealEstateTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PropertyCardTest {

    @get:Rule
    val composeRule = createComposeRule()

    private val targetContext = InstrumentationRegistry.getInstrumentation().targetContext

    private fun sampleProperty(bookmarked: Boolean) = Property(
        id = "1",
        title = "Sample",
        imageUrl = null,
        price = Price(100L, "CHF"),
        address = Address("Street", "8000", "Town"),
        listingType = ListingTier.TOP,
        isBookmarked = bookmarked,
    )

    @Test
    fun notBookmarkedShowsAddDescription() {
        composeRule.setContent {
            SMGRealEstateTheme {
                PropertyCard(
                    property = sampleProperty(bookmarked = false),
                    onBookmarkClick = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(targetContext.getString(R.string.cd_bookmark_add))
            .assertIsDisplayed()
    }

    @Test
    fun bookmarkedShowsRemoveDescription() {
        composeRule.setContent {
            SMGRealEstateTheme {
                PropertyCard(
                    property = sampleProperty(bookmarked = true),
                    onBookmarkClick = {},
                )
            }
        }

        composeRule
            .onNodeWithContentDescription(targetContext.getString(R.string.cd_bookmark_remove))
            .assertIsDisplayed()
    }
}
