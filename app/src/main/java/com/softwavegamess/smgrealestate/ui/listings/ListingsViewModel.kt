package com.softwavegamess.smgrealestate.ui.listings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softwavegamess.smgrealestate.R
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.domain.usecase.GetPropertiesUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class ListingsViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val getProperties: GetPropertiesUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : ViewModel() {

    private val loaded = MutableStateFlow<List<Property>>(emptyList())
    private val searchQuery = MutableStateFlow("")
    private val isLoading = MutableStateFlow(true)
    private val loadError = MutableStateFlow<String?>(null)
    private val remoteListWasEmpty = MutableStateFlow(false)

    private val _state = MutableStateFlow(ListingsUiState())
    val state: StateFlow<ListingsUiState> = _state.asStateFlow()

    private val _userMessages = Channel<String>(Channel.BUFFERED)
    val userMessages = _userMessages.receiveAsFlow()

    init {
        viewModelScope.launch {
            combine(
                loaded,
                searchQuery,
                isLoading,
                loadError,
                remoteListWasEmpty,
            ) { loadedList, query, loading, err, remoteEmpty ->
                val filtered = when {
                    loading || err != null -> emptyList()
                    else -> loadedList.matchingSearch(query)
                }
                ListingsUiState(
                    isLoading = loading,
                    loadError = err,
                    searchQuery = query,
                    properties = filtered,
                    remoteListWasEmpty = remoteEmpty,
                )
            }.collect { _state.value = it }
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

    fun onBookmarkClicked(property: Property) {
        viewModelScope.launch {
            val snapshot = loaded.value
            val optimistic = snapshot.map { item ->
                if (item.id == property.id) item.copy(isBookmarked = !item.isBookmarked) else item
            }
            loaded.update { optimistic }

            toggleBookmark(property.id).fold(
                onSuccess = { bookmarked ->
                    loaded.update { list ->
                        list.map { item ->
                            if (item.id == property.id) item.copy(isBookmarked = bookmarked) else item
                        }
                    }
                },
                onFailure = {
                    loaded.value = snapshot
                    _userMessages.trySend(appContext.getString(R.string.bookmark_update_failed))
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
