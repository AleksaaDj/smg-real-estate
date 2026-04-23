package com.softwavegamess.smgrealestate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.softwavegamess.smgrealestate.ui.listings.ListingsScaffold
import com.softwavegamess.smgrealestate.ui.theme.SMGRealEstateTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SMGRealEstateTheme {
                ListingsScaffold()
            }
        }
    }
}
