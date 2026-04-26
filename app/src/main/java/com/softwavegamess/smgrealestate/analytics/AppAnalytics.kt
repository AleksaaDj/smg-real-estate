package com.softwavegamess.smgrealestate.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAnalytics @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val firebase = FirebaseAnalytics.getInstance(context)

    fun logListingSearch(queryLength: Int) {
        if (queryLength <= 0) return
        firebase.logEvent(
            "listing_search",
            Bundle().apply { putLong("query_length", queryLength.toLong()) },
        )
    }

    fun logBookmarkToggle(listingId: String, bookmarked: Boolean) {
        firebase.logEvent(
            "bookmark_toggle",
            Bundle().apply {
                putString("listing_id", listingId)
                putBoolean("is_bookmarked", bookmarked)
            },
        )
    }
}
