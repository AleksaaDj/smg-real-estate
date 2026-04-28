package com.softwavegamess.smgrealestate.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
enum class ListingTier : Parcelable {
    TOP,
    STANDARD,
    BASIC,
    UNKNOWN,
}
