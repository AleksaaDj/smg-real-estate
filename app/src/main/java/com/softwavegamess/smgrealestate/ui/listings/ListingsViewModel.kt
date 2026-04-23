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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException

@HiltViewModel
class ListingsViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val getProperties: GetPropertiesUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(ListingsUiState())
    val state: StateFlow<ListingsUiState> = _state.asStateFlow()

    private val _userMessages = Channel<String>(Channel.BUFFERED)
    val userMessages = _userMessages.receiveAsFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = ListingsUiState(isLoading = true, properties = emptyList(), loadError = null)
            getProperties().fold(
                onSuccess = { list ->
                    _state.value = ListingsUiState(
                        isLoading = false,
                        properties = list,
                        loadError = null,
                    )
                },
                onFailure = { error ->
                    _state.value = ListingsUiState(
                        isLoading = false,
                        properties = emptyList(),
                        loadError = error.toLoadMessage(),
                    )
                },
            )
        }
    }

    fun onBookmarkClicked(property: Property) {
        viewModelScope.launch {
            val snapshot = _state.value.properties
            val optimistic = snapshot.map { item ->
                if (item.id == property.id) item.copy(isBookmarked = !item.isBookmarked) else item
            }
            _state.setProperties(optimistic)

            toggleBookmark(property.id).fold(
                onSuccess = { bookmarked ->
                    _state.setProperties(
                        _state.value.properties.map { item ->
                            if (item.id == property.id) item.copy(isBookmarked = bookmarked) else item
                        },
                    )
                },
                onFailure = {
                    _state.setProperties(snapshot)
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

    private fun MutableStateFlow<ListingsUiState>.setProperties(properties: List<Property>) {
        value = value.copy(properties = properties)
    }
}
