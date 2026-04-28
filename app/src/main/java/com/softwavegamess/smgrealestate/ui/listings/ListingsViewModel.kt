package com.softwavegamess.smgrealestate.ui.listings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softwavegamess.smgrealestate.R
import com.softwavegamess.smgrealestate.analytics.AppAnalytics
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.domain.usecase.GetPropertiesUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ObserveBookmarkedPropertyIdsUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

private const val SearchAnalyticsDebounceMs = 350L
private const val StateSubscriptionStopTimeoutMs = 5_000L

private data class ListingsSearchState(
    val properties: List<Property>,
    val searchQuery: String,
    val sort: ListingSortOption,
    val bookmarkedIds: Set<String>,
)

private data class ListingsLoadFlags(
    val isLoading: Boolean,
    val loadError: String?,
    val remoteListWasEmpty: Boolean,
)

@OptIn(FlowPreview::class)
@HiltViewModel
class ListingsViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val getProperties: GetPropertiesUseCase,
    observeBookmarkedIds: ObserveBookmarkedPropertyIdsUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
    private val analytics: AppAnalytics,
) : ViewModel() {

    private val loaded = MutableStateFlow<List<Property>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val sortOption = MutableStateFlow(ListingSortOption.DEFAULT)
    private val isLoading = MutableStateFlow(true)
    private val loadError = MutableStateFlow<String?>(null)
    private val remoteListWasEmpty = MutableStateFlow(false)

    val state: StateFlow<ListingsUiState> = combine(
        combine(loaded, searchQuery, sortOption, observeBookmarkedIds()) { l, q, s, bookmarked ->
            ListingsSearchState(l, q, s, bookmarked)
        },
        combine(isLoading, loadError, remoteListWasEmpty) { loading, err, re ->
            ListingsLoadFlags(loading, err, re)
        },
    ) { search, flags ->
        val withBookmarks = search.properties.map { p ->
            p.copy(isBookmarked = search.bookmarkedIds.contains(p.id))
        }
        val filtered = when {
            flags.isLoading || flags.loadError != null -> emptyList()
            else -> withBookmarks.matchingSearch(search.searchQuery)
        }
        val displayed = filtered.sortedByOption(search.sort)
        ListingsUiState(
            isLoading = flags.isLoading,
            loadError = flags.loadError,
            searchQuery = search.searchQuery,
            properties = displayed,
            remoteListWasEmpty = flags.remoteListWasEmpty,
            sortOption = search.sort,
        )
    }
        .flowOn(Dispatchers.Default)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(
                stopTimeoutMillis = StateSubscriptionStopTimeoutMs,
            ),
            ListingsUiState(),
        )

    private val _userMessages = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val userMessages = _userMessages.asSharedFlow()

    init {
        viewModelScope.launch {
            searchQuery
                .debounce(SearchAnalyticsDebounceMs)
                .distinctUntilChanged()
                .collect { q ->
                    if (q.isNotBlank()) {
                        analytics.logListingSearch(q.length)
                    }
                }
        }
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading.value = true
            loadError.value = null
            getProperties().fold(
                onSuccess = { list ->
                    loaded.value = list
                    remoteListWasEmpty.value = list.isEmpty()
                    isLoading.value = false
                },
                onFailure = { error ->
                    loaded.value = emptyList()
                    remoteListWasEmpty.value = false
                    loadError.value = error.toLoadMessage()
                    isLoading.value = false
                },
            )
        }
    }

    fun onSearchQueryChange(raw: String) {
        searchQuery.value = raw
    }

    fun onSortOptionChange(option: ListingSortOption) {
        if (option == sortOption.value) return
        sortOption.value = option
        analytics.logSortChanged(option.analyticsKey())
    }

    private fun ListingSortOption.analyticsKey(): String = when (this) {
        ListingSortOption.DEFAULT -> "default"
        ListingSortOption.PRICE_ASC -> "price_asc"
        ListingSortOption.PRICE_DESC -> "price_desc"
        ListingSortOption.TITLE_A_Z -> "title_a_z"
    }

    fun onBookmarkClicked(property: Property) {
        viewModelScope.launch {
            val snapshot = loaded.value
            val optimistic = snapshot.map { item ->
                if (item.id == property.id) item.copy(isBookmarked = !item.isBookmarked) else item
            }
            loaded.update { optimistic }

            toggleBookmark(property.id).fold(
                onSuccess = { bookmarked ->
                    analytics.logBookmarkToggle(property.id, bookmarked)
                    loaded.update { list ->
                        list.map { item ->
                            if (item.id == property.id) item.copy(isBookmarked = bookmarked) else item
                        }
                    }
                },
                onFailure = {
                    loaded.value = snapshot
                    _userMessages.tryEmit(appContext.getString(R.string.bookmark_update_failed))
                },
            )
        }
    }

    private fun Throwable.toLoadMessage(): String = when (this) {
        is IOException -> appContext.getString(R.string.listings_error_network)
        is HttpException -> appContext.getString(R.string.listings_error_http)
        else -> appContext.getString(R.string.listings_error_unknown)
    }
}
