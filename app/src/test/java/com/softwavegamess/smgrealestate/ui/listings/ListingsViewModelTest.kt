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
import com.softwavegamess.smgrealestate.domain.usecase.GetPropertiesUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ToggleBookmarkUseCase
import io.mockk.coEvery
import io.mockk.mockk
import java.io.IOException
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

        val viewModel = ListingsViewModel(context, get, toggle)

        assertFalse(viewModel.state.value.isLoading)
        assertNull(viewModel.state.value.loadError)
        assertEquals(listOf(sampleProperty), viewModel.state.value.properties)
    }

    @Test
    fun loadFailureSetsNetworkMessage() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.failure(IOException())
        val toggle = mockk<ToggleBookmarkUseCase>(relaxed = true)

        val viewModel = ListingsViewModel(context, get, toggle)

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

        val viewModel = ListingsViewModel(context, get, toggle)

        assertEquals(context.getString(R.string.listings_error_http), viewModel.state.value.loadError)
    }

    @Test
    fun bookmarkFailureRevertsAndEmitsMessage() = runTest {
        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns Result.success(listOf(sampleProperty))
        val toggle = mockk<ToggleBookmarkUseCase>()
        coEvery { toggle(sampleProperty.id) } returns Result.failure(RuntimeException("db"))

        val viewModel = ListingsViewModel(context, get, toggle)

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

        val viewModel = ListingsViewModel(context, get, toggle)

        viewModel.onBookmarkClicked(sampleProperty)

        assertTrue(viewModel.state.value.properties.single().isBookmarked)
    }
}
