package com.softwavegamess.smgrealestate.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.softwavegamess.smgrealestate.R
import com.softwavegamess.smgrealestate.domain.model.Address
import com.softwavegamess.smgrealestate.domain.model.ListingTier
import com.softwavegamess.smgrealestate.domain.model.Price
import com.softwavegamess.smgrealestate.domain.model.Property
import com.softwavegamess.smgrealestate.ui.theme.SMGRealEstateTheme
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PropertyCard(
    property: Property,
    onBookmarkClick: () -> Unit,
    onOpenDetails: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder(),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f),
            ) {
                if (property.imageUrl.isNullOrBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                    )
                } else {
                    AsyncImage(
                        model = property.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 0.dp,
                ) {
                    Text(
                        text = formatPrice(property.price),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp),
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.45f),
                ) {
                    IconButton(
                        onClick = onBookmarkClick,
                        modifier = Modifier.size(48.dp),
                    ) {
                        Icon(
                            imageVector = if (property.isBookmarked) {
                                Icons.Filled.Favorite
                            } else {
                                Icons.Filled.FavoriteBorder
                            },
                            contentDescription = stringResource(
                                if (property.isBookmarked) {
                                    R.string.cd_bookmark_remove
                                } else {
                                    R.string.cd_bookmark_add
                                },
                            ),
                            tint = if (property.isBookmarked) {
                                MaterialTheme.colorScheme.error
                            } else {
                                Color.White
                            },
                        )
                    }
                }
            }

            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                Text(
                    text = property.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF00838F),
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.size(4.dp))
                    Text(
                        text = property.address?.toSingleLine().orEmpty(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun formatPrice(price: Price?): String {
    val amount = price?.amount
    val currency = price?.currencyCode?.takeIf { it.isNotBlank() }
    if (amount == null || currency == null) {
        return stringResource(R.string.price_on_request)
    }
    val formatter = remember(currency) {
        NumberFormat.getNumberInstance(Locale.forLanguageTag("de-CH"))
    }
    return "$currency ${formatter.format(amount)}"
}

private fun previewSampleProperty(
    bookmarked: Boolean,
    title: String = "Modern apartment with balcony",
) = Property(
    id = "preview-1",
    title = title,
    imageUrl = null,
    price = Price(985_000L, "CHF"),
    address = Address("Seestrasse 42", "8008", "Zürich"),
    listingType = ListingTier.STANDARD,
    isBookmarked = bookmarked,
)

@Preview(name = "Not bookmarked", showBackground = true)
@Composable
private fun PropertyCardNotBookmarkedPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        PropertyCard(
            property = previewSampleProperty(bookmarked = false),
            onBookmarkClick = {},
            onOpenDetails = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview(name = "Bookmarked", showBackground = true)
@Composable
private fun PropertyCardBookmarkedPreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        PropertyCard(
            property = previewSampleProperty(bookmarked = true),
            onBookmarkClick = {},
            onOpenDetails = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}

@Preview(name = "Long title", showBackground = true, widthDp = 320)
@Composable
private fun PropertyCardLongTitlePreview() {
    SMGRealEstateTheme(dynamicColor = false) {
        PropertyCard(
            property = previewSampleProperty(
                bookmarked = false,
                title = "Exceptional waterfront residence with private garden and panoramic alpine views",
            ),
            onBookmarkClick = {},
            onOpenDetails = {},
            modifier = Modifier.padding(12.dp),
        )
    }
}
