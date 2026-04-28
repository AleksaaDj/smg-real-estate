package com.softwavegamess.smgrealestate.ui.listings

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
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
    onOpenDetails: (Property) -> Unit,
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
        onOpenDetails = onOpenDetails,
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSortOptionChange = viewModel::onSortOptionChange,
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
    onOpenDetails: (Property) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSortOptionChange: (ListingSortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(text = stringResource(R.string.screen_listings_title)) },
            )
        },
    ) { innerPadding ->
        when {
            state.loadError != null -> {
                ListingsEmptyBlock(
                    message = state.loadError,
                    onRetry = onRetry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                )
            }

            else -> {
                val listState = rememberLazyListState()
                LaunchedEffect(state.sortOption, state.searchQuery) {
                    if (state.properties.isNotEmpty()) {
                        listState.scrollToItem(0)
                    }
                }
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    ListingsSearchWithSort(
                        query = state.searchQuery,
                        onQueryChange = onSearchQueryChange,
                        currentSort = state.sortOption,
                        onSortOptionChange = onSortOptionChange,
                    )
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                    )
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                    ) {
                        when {
                            state.isLoading -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }

                            state.properties.isEmpty() -> {
                                val message = when {
                                    state.remoteListWasEmpty ->
                                        stringResource(R.string.listings_empty)
                                    state.searchQuery.isNotBlank() ->
                                        stringResource(R.string.listings_search_none)
                                    else -> stringResource(R.string.listings_empty)
                                }
                                ListingsEmptyBlock(
                                    message = message,
                                    onRetry = onRetry,
                                    modifier = Modifier.fillMaxSize(),
                                )
                            }

                            else -> {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(
                                        start = 12.dp,
                                        end = 12.dp,
                                        top = 4.dp,
                                        bottom = 12.dp,
                                    ),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    items(
                                        items = state.properties,
                                        key = { it.id },
                                    ) { property ->
                                        PropertyCard(
                                            property = property,
                                            onBookmarkClick = { onBookmarkClick(property) },
                                            onOpenDetails = { onOpenDetails(property) },
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingsSearchWithSort(
    query: String,
    onQueryChange: (String) -> Unit,
    currentSort: ListingSortOption,
    onSortOptionChange: (ListingSortOption) -> Unit,
) {
    var showSortSheet by rememberSaveable { mutableStateOf(false) }
    var fieldValue by rememberSaveable(stateSaver = TextFieldValue.Saver) {
        mutableStateOf(TextFieldValue(query))
    }

    LaunchedEffect(query) {
        if (query != fieldValue.text) {
            fieldValue = fieldValue.copy(text = query)
        }
    }
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = fieldValue,
            onValueChange = { next ->
                fieldValue = next
                if (next.text != query) {
                    onQueryChange(next.text)
                }
            },
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = stringResource(R.string.listings_search_hint),
                    style = MaterialTheme.typography.bodyLarge,
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.cd_search_clear),
                        )
                    }
                }
            },
            singleLine = true,
            shape = shape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            ),
        )
        FilledTonalIconButton(
            onClick = { showSortSheet = true },
        ) {
            Icon(
                imageVector = Icons.Filled.SwapVert,
                contentDescription = stringResource(R.string.cd_sort_menu),
            )
        }
    }
    if (showSortSheet) {
        ListingsSortBottomSheet(
            currentSort = currentSort,
            onOptionSelected = { option ->
                onSortOptionChange(option)
                showSortSheet = false
            },
            onDismiss = { showSortSheet = false },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListingsSortBottomSheet(
    currentSort: ListingSortOption,
    onOptionSelected: (ListingSortOption) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Text(
            text = stringResource(R.string.listings_sort_sheet_title),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        )
        Spacer(Modifier.height(4.dp))
        ListingSortOption.entries.forEach { option ->
            val selected = option == currentSort
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOptionSelected(option) }
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(option.sortLabelRes()),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

private fun ListingSortOption.sortLabelRes(): Int = when (this) {
    ListingSortOption.DEFAULT -> R.string.sort_default
    ListingSortOption.PRICE_ASC -> R.string.sort_price_asc
    ListingSortOption.PRICE_DESC -> R.string.sort_price_desc
    ListingSortOption.TITLE_A_Z -> R.string.sort_title_az
}

@Composable
private fun ListingsEmptyBlock(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message,
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
            onOpenDetails = {},
            onSearchQueryChange = {},
            onSortOptionChange = {},
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
            onOpenDetails = {},
            onSearchQueryChange = {},
            onSortOptionChange = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Listings — empty", showBackground = true)
@Composable
private fun ListingsScreenEmptyPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        ListingsScreen(
            state = ListingsUiState(
                isLoading = false,
                properties = emptyList(),
                loadError = null,
                remoteListWasEmpty = true,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onBookmarkClick = {},
            onOpenDetails = {},
            onSearchQueryChange = {},
            onSortOptionChange = {},
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
            onOpenDetails = {},
            onSearchQueryChange = {},
            onSortOptionChange = {},
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
            onOpenDetails = {},
            onSearchQueryChange = {},
            onSortOptionChange = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Listings — search miss", showBackground = true)
@Composable
private fun ListingsScreenSearchMissPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        ListingsScreen(
            state = ListingsUiState(
                isLoading = false,
                properties = emptyList(),
                loadError = null,
                searchQuery = "zzz",
                remoteListWasEmpty = false,
            ),
            snackbarHostState = remember { SnackbarHostState() },
            onRetry = {},
            onBookmarkClick = {},
            onOpenDetails = {},
            onSearchQueryChange = {},
            onSortOptionChange = {},
        )
    }
}
