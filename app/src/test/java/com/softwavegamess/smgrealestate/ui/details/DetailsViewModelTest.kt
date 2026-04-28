package com.softwavegamess.smgrealestate.ui.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import com.softwavegamess.smgrealestate.MainDispatcherRule
import com.softwavegamess.smgrealestate.R
import com.softwavegamess.smgrealestate.domain.model.Address
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Price
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.domain.usecase.GetPropertiesUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ObserveBookmarkedPropertyIdsUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ToggleBookmarkUseCase
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private val propertyId = "p1"

    private val sample = Property(
        id = propertyId,
        title = "Lake view",
        imageUrl = null,
        price = Price(100L, "CHF"),
        address = Address("Street", "8000", "Town"),
        listingType = ListingTier.TOP,
        isBookmarked = false,
    )

    private fun createViewModel(
        bookmarkedIds: MutableStateFlow<Set<String>> = MutableStateFlow(emptySet()),
        getPropertiesResult: Result<List<Property>> = Result.success(emptyList()),
        toggleResult: Result<Boolean> = Result.success(true),
    ): DetailsViewModel {
        val savedStateHandle = SavedStateHandle(mapOf("propertyId" to propertyId))

        val get = mockk<GetPropertiesUseCase>()
        coEvery { get() } returns getPropertiesResult

        val toggle = mockk<ToggleBookmarkUseCase>()
        coEvery { toggle(propertyId) } returns toggleResult

        val observe = mockk<ObserveBookmarkedPropertyIdsUseCase>()
        every { observe() } returns bookmarkedIds

        return DetailsViewModel(
            appContext = context,
            savedStateHandle = savedStateHandle,
            getProperties = get,
            observeBookmarkedIds = observe,
            toggleBookmark = toggle,
        )
    }

    @Test
    fun seedInitialProperty_usesSnapshot_andAppliesBookmarkFlow() = runTest {
        val bookmarkedIds = MutableStateFlow(emptySet<String>())
        val vm = createViewModel(bookmarkedIds = bookmarkedIds)

        vm.seedInitialProperty(sample)
        vm.state.filter { it.property != null }.first()

        assertNotNull(vm.state.value.property)
        assertFalse(vm.state.value.property!!.isBookmarked)

        bookmarkedIds.value = setOf(propertyId)
        vm.state.filter { it.property?.isBookmarked == true }.first()

        assertTrue(vm.state.value.property!!.isBookmarked)
        assertNull(vm.state.value.errorMessage)
        assertFalse(vm.state.value.isLoading)
    }

    @Test
    fun seedInitialProperty_fallbackFindsById_whenNoSnapshot() = runTest {
        val vm = createViewModel(
            getPropertiesResult = Result.success(listOf(sample)),
        )

        vm.seedInitialProperty(null)
        vm.state.filter { !it.isLoading }.first()

        assertNull(vm.state.value.errorMessage)
        assertEquals(propertyId, vm.state.value.property?.id)
    }

    @Test
    fun seedInitialProperty_setsNotFoundMessage_whenMissing() = runTest {
        val vm = createViewModel(
            getPropertiesResult = Result.success(emptyList()),
        )

        vm.seedInitialProperty(null)
        vm.state.filter { !it.isLoading }.first()

        assertNull(vm.state.value.property)
        assertEquals(context.getString(R.string.details_not_found), vm.state.value.errorMessage)
    }

    @Test
    fun onBookmarkClicked_success_setsFinalBookmarkedValue() = runTest {
        val vm = createViewModel(toggleResult = Result.success(true))

        vm.seedInitialProperty(sample)
        vm.state.filter { it.property != null }.first()

        vm.onBookmarkClicked()
        vm.state.filter { !it.isBookmarkUpdating }.first()

        assertTrue(vm.state.value.property!!.isBookmarked)
        assertNull(vm.state.value.errorMessage)
    }

    @Test
    fun onBookmarkClicked_failure_revertsAndEmitsMessage() = runTest {
        val vm = createViewModel(toggleResult = Result.failure(IOException()))

        vm.seedInitialProperty(sample.copy(isBookmarked = false))
        vm.state.filter { it.property != null }.first()

        vm.userMessages.test {
            vm.onBookmarkClicked()
            assertEquals(context.getString(R.string.bookmark_update_failed), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }

        vm.state.filter { !it.isBookmarkUpdating }.first()
        assertFalse(vm.state.value.property!!.isBookmarked)
    }
}

