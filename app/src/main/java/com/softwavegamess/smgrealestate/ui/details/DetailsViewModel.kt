package com.softwavegamess.smgrealestate.ui.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.softwavegamess.smgrealestate.R
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.domain.usecase.GetPropertiesUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ObserveBookmarkedPropertyIdsUseCase
import com.softwavegamess.smgrealestate.domain.usecase.ToggleBookmarkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val ArgPropertyId = "propertyId"

@HiltViewModel
class DetailsViewModel @Inject constructor(
    @param:ApplicationContext private val appContext: Context,
    savedStateHandle: SavedStateHandle,
    private val getProperties: GetPropertiesUseCase,
    private val observeBookmarkedIds: ObserveBookmarkedPropertyIdsUseCase,
    private val toggleBookmark: ToggleBookmarkUseCase,
) : ViewModel() {

    private val propertyId: String = savedStateHandle.get<String>(ArgPropertyId).orEmpty()

    private val _state = MutableStateFlow(PropertyDetailsUiState(isLoading = true))
    val state: StateFlow<PropertyDetailsUiState> = _state

    private val _userMessages = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val userMessages = _userMessages.asSharedFlow()

    private var seeded = false
    private var latestBookmarkedIds: Set<String> = emptySet()

    init {
        viewModelScope.launch {
            observeBookmarkedIds().collect { ids ->
                latestBookmarkedIds = ids
                _state.update { st ->
                    val p = st.property ?: return@update st
                    st.copy(property = p.copy(isBookmarked = ids.contains(p.id)))
                }
            }
        }
    }

    fun seedInitialProperty(initial: Property?) {
        if (seeded) return
        seeded = true

        if (initial != null) {
            _state.update {
                it.copy(
                    isLoading = false,
                    property = initial.copy(isBookmarked = latestBookmarkedIds.contains(initial.id)),
                    errorMessage = null,
                )
            }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            getProperties().fold(
                onSuccess = { list ->
                    val found = list.firstOrNull { it.id == propertyId }
                    _state.update {
                        it.copy(
                            isLoading = false,
                            property = found?.copy(isBookmarked = latestBookmarkedIds.contains(found.id)),
                            errorMessage = if (found == null) {
                                appContext.getString(R.string.details_not_found)
                            } else {
                                null
                            },
                        )
                    }
                },
                onFailure = {
                    _state.update {
                        it.copy(
                            isLoading = false,
                            property = null,
                            errorMessage = appContext.getString(R.string.details_not_found),
                        )
                    }
                },
            )
        }
    }

    fun onBookmarkClicked() {
        val current = _state.value.property ?: return
        if (_state.value.isBookmarkUpdating) return

        viewModelScope.launch {
            val snapshot = current
            _state.update {
                it.copy(
                    isBookmarkUpdating = true,
                    property = snapshot.copy(isBookmarked = !snapshot.isBookmarked),
                )
            }

            toggleBookmark(snapshot.id).fold(
                onSuccess = { bookmarked ->
                    _state.update {
                        it.copy(
                            isBookmarkUpdating = false,
                            property = snapshot.copy(isBookmarked = bookmarked),
                        )
                    }
                },
                onFailure = {
                    _state.update { it.copy(isBookmarkUpdating = false, property = snapshot) }
                    _userMessages.tryEmit(appContext.getString(R.string.bookmark_update_failed))
                },
            )
        }
    }
}

