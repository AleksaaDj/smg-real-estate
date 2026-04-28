package com.softwavegamess.smgrealestate.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Property(
    val id: String,
    val title: String,
    val imageUrl: String?,
    val price: Price?,
    val address: Address?,
    val listingType: ListingTier,
    val isBookmarked: Boolean = false,
) : Parcelable
