package com.softwavegamess.smgrealestate.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Price(
    val amount: Long?,
    val currencyCode: String,
) : Parcelable
