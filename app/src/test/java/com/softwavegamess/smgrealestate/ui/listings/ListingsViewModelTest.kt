package com.softwavegamess.smgrealestate.ui.listings

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.softwavegamess.smgrealestate.MainDispatcherRule
import com.softwavegamess.smgrealestate.R
import com.softwavegamess.smgrealestate.domain.model.Address
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Price
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.analytics.AppAnalytics
import com.softwavegamess.smgrealestate.domain.usecase.GetPropertiesUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ToggleBookmarkUseCase
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.verify
import java.io.IOException
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import retrofit2.HttpException
import retrofit2.Response

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ListingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val analytics: AppAnalytics = mockk(relaxed = true)

    private val sampleProperty = Property(
        id = "1",
        title = "Title",
        imageUrl = null,
        price = Price(100L, "CHF"),
        address = Address("Street", "8000", "Town"),
        listingType = ListingTier.TOP,
        isBookmarked = false,
    )

    @Test
    fun loadSuccessExposesProperties() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(listOf(sampleProperty))
        val toggle = mockk<ToggleBookmarkUseCase>(relaxed = true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.loadError)
        assertEquals(listOf(sampleProperty), viewModel.state.value.properties)
    }

    @Test
    fun loadFailureSetsNetworkMessage() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.failure(IOException())
        val toggle = mockk<ToggleBookmarkUseCase>(relaxed = true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        assertFalse(viewModel.state.value.isLoading)
        assertEquals(context.getString(R.string.listings_error_network), viewModel.state.value.loadError)
        assertTrue(viewModel.state.value.properties.isEmpty())
    }

    @Test
    fun loadFailureHttpUsesHttpMessage() = runTest {
        val body = "{}".toResponseBody("application/json".toMediaType())
        val response = Response.error<Any>(500, body)
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.failure(HttpException(response))
        val toggle = mockk<ToggleBookmarkUseCase>(relaxed = true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        assertEquals(context.getString(R.string.listings_error_http), viewModel.state.value.loadError)
    }

    @Test
    fun bookmarkFailureRevertsAndEmitsMessage() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(listOf(sampleProperty))
        val toggle = mockk<ToggleBookmarkUseCase>()
        coEvery { toggle(sampleProperty.id) } returns Result.failure(RuntimeException("db"))

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        viewModel.userMessages.test {
            viewModel.onBookmarkClicked(sampleProperty)
            assertEquals(context.getString(R.string.bookmark_update_failed), awaitItem())
        }

        assertFalse(viewModel.state.value.properties.single().isBookmarked)
    }

    @Test
    fun bookmarkSuccessKeepsToggledState() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(listOf(sampleProperty))
        val toggle = mockk<ToggleBookmarkUseCase>()
        coEvery { toggle(sampleProperty.id) } returns Result.success(true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        viewModel.onBookmarkClicked(sampleProperty)
        viewModel.state.filter { it.properties.isNotEmpty() && it.properties.single().isBookmarked }
            .first()

        assertTrue(viewModel.state.value.properties.single().isBookmarked)
    }

    @Test
    fun searchNarrowsList() = runTest {
        val a = sampleProperty.copy(id = "1", title = "Lake view")
        val b = sampleProperty.copy(id = "2", title = "City studio")
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(listOf(a, b))
        val toggle = mockk<ToggleBookmarkUseCase>(relaxed = true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        viewModel.onSearchQueryChange("studio")
        viewModel.state
            .filter { it.searchQuery == "studio" && it.properties == listOf(b) }
            .first()

        assertEquals(listOf(b), viewModel.state.value.properties)
    }

    @Test
    fun loadSuccessEmptyMarksRemoteEmpty() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(emptyList())
        val toggle = mockk<ToggleBookmarkUseCase>(relaxed = true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        assertTrue(viewModel.state.value.remoteListWasEmpty)
        assertTrue(viewModel.state.value.properties.isEmpty())
    }

    @Test
    fun bookmarkWorksWhileSearchActive() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(listOf(sampleProperty))
        val toggle = mockk<ToggleBookmarkUseCase>()
        coEvery { toggle(sampleProperty.id) } returns Result.success(true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()
        viewModel.onSearchQueryChange("Title")
        viewModel.state
            .filter { it.searchQuery == "Title" }
            .first()

        viewModel.onBookmarkClicked(sampleProperty)
        viewModel.state
            .filter { it.properties.single().isBookmarked }
            .first()

        assertTrue(viewModel.state.value.properties.single().isBookmarked)
    }

    @Test
    fun sortPriceAscendingReordersList() = runTest {
        val cheap = sampleProperty.copy(id = "1", title = "A", price = Price(100L, "CHF"))
        val expensive = sampleProperty.copy(id = "2", title = "B", price = Price(500L, "CHF"))
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(listOf(expensive, cheap))
        val toggle = mockk<ToggleBookmarkUseCase>(relaxed = true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        viewModel.onSortOptionChange(ListingSortOption.PRICE_ASC)
        viewModel.state
            .filter { it.sortOption == ListingSortOption.PRICE_ASC }
            .first()

        assertEquals(listOf(cheap, expensive), viewModel.state.value.properties)
    }

    @Test
    fun sortChangeFiresAnalyticsOnce() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(listOf(sampleProperty))
        val toggle = mockk<ToggleBookmarkUseCase>(relaxed = true)

        val viewModel = ListingsViewModel(context, get, toggle, analytics)
        viewModel.awaitListingsReady()

        viewModel.onSortOptionChange(ListingSortOption.PRICE_DESC)
        viewModel.onSortOptionChange(ListingSortOption.PRICE_DESC)

        verify(exactly = 1) { analytics.logSortChanged("price_desc") }
    }

    private suspend fun ListingsViewModel.awaitListingsReady() {
        state.filter { !it.isLoading }.first()
    }
}
