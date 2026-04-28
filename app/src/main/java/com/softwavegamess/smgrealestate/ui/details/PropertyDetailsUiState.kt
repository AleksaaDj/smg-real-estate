package com.softwavegamess.smgrealestate.ui.details

import com.softwavegamess.smgrealestate.domain.model.Property

data class PropertyDetailsUiState(
    val isLoading: Boolean = false,
    val property: Property? = null,
    val errorMessage: String? = null,
    val isBookmarkUpdating: Boolean = false,
)

