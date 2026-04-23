package com.softwavegamess.smgrealestate.ui.listings

import com.softwavegamess.smgrealestate.domain.model.Property

data class ListingsUiState(
    val isLoading: Boolean = true,
    val properties: List<Property> = emptyList(),
    val loadError: String? = null,
)
