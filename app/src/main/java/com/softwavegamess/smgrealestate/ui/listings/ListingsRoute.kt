package com.softwavegamess.smgrealestate.ui.listings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.softwavegamess.smgrealestate.R
import com.softwavegamess.smgrealestate.domain.model.Address
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Price
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.ui.components.PropertyCard
import com.softwavegamess.smgrealestate.ui.theme.SMGRealEstateTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsRoute(
    modifier: Modifier = Modifier,
    viewModel: ListingsViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.userMessages.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    ListingsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onRetry = viewModel::load,
        onBookmarkClick = viewModel::onBookmarkClicked,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingsScreen(
    state: ListingsUiState,
    snackbarHostState: SnackbarHostState,
    onRetry: () -> Unit,
    onBookmarkClick: (Property) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(title = { Text(text = stringResource(R.string.screen_listings_title)) })
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.loadError != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = state.loadError,
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            Text(text = stringResource(R.string.action_retry))
                        }
                    }
                }

                state.properties.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.listings_empty),
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(
                            onClick = onRetry,
                            modifier = Modifier.padding(top = 16.dp),
                        ) {
                            Text(text = stringResource(R.string.action_retry))
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = state.properties,
                            key = { it.id },
                        ) { property ->
                            PropertyCard(
                                property = property,
                                onBookmarkClick = { onBookmarkClick(property) },
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun previewProperty(
    id: String,
    title: String,
    bookmarked: Boolean,
) = Property(
    id = id,
    title = title,
    imageUrl = null,
    price = Price(1_250_000L, "CHF"),
    address = Address("Bahnhofstrasse 1", "8001", "Zürich"),
    listingType = ListingTier.TOP,
    isBookmarked = bookmarked,
)

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Listings — loading", showBackground = true)
@Composable
private fun ListingsScreenLoadingPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        ListingsScreen(
            state = ListingsUiState(isLoading = true, properties = emptyList(), loadError = null),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onBookmarkClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Listings — error", showBackground = true)
@Composable
private fun ListingsScreenErrorPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        ListingsScreen(
            state = ListingsUiState(
                isLoading = false,
                properties = emptyList(),
                loadError = "Could not load properties.",
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onBookmarkClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Listings — empty", showBackground = true)
@Composable
private fun ListingsScreenEmptyPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        ListingsScreen(
            state = ListingsUiState(isLoading = false, properties = emptyList(), loadError = null),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onBookmarkClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Listings — list", showBackground = true)
@Composable
private fun ListingsScreenListPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        ListingsScreen(
            state = ListingsUiState(
                isLoading = false,
                properties = listOf(
                    previewProperty("1", "Lake view penthouse", bookmarked = false),
                    previewProperty("2", "Cozy studio near the station", bookmarked = true),
                ),
                loadError = null,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onBookmarkClick = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(
    name = "Listings — list (dark)",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun ListingsScreenListDarkPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        ListingsScreen(
            state = ListingsUiState(
                isLoading = false,
                properties = listOf(
                    previewProperty("1", "Lake view penthouse", bookmarked = true),
                ),
                loadError = null,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onBookmarkClick = {},
        )
    }
}
