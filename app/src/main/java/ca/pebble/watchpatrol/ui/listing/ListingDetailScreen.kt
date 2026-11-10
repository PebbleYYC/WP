package ca.pebble.watchpatrol.ui.listing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.pebble.watchpatrol.di.wpViewModel
import ca.pebble.watchpatrol.domain.model.Listing
import ca.pebble.watchpatrol.ui.components.ErrorState
import ca.pebble.watchpatrol.ui.components.LoadingState
import ca.pebble.watchpatrol.ui.components.SourceChip
import ca.pebble.watchpatrol.ui.components.WpAsyncImage
import ca.pebble.watchpatrol.ui.components.WpTopBar
import ca.pebble.watchpatrol.util.Async
import ca.pebble.watchpatrol.util.openUrl

@Composable
fun ListingDetailScreen(
    listingId: Long,
    onBack: () -> Unit,
    onOpenModel: (String) -> Unit,
) {
    val vm = wpViewModel { ListingDetailViewModel(it) }
    LaunchedEffect(listingId) { vm.load(listingId) }
    val state by vm.state.collectAsStateWithLifecycle()
    val starredIds by vm.starredIds.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize()) {
        WpTopBar(
            title = "Listing",
            onBack = onBack,
            actions = {
                (state as? Async.Success)?.data?.let { listing ->
                    val starred = listing.id in starredIds
                    IconButton(onClick = { vm.toggleStar(listing) }) {
                        Icon(
                            imageVector = if (starred) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (starred) "Unstar" else "Star",
                            tint = if (starred) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            },
        )
        when (val s = state) {
            Async.Loading -> LoadingState()
            is Async.Error -> ErrorState(s.message, onRetry = { vm.load(listingId) })
            is Async.Success -> ListingDetailContent(
                listing = s.data,
                onOpenOriginal = { openUrl(context, s.data.originalUrl) },
                onOpenModel = onOpenModel,
            )
        }
    }
}

@Composable
private fun ListingDetailContent(
    listing: Listing,
    onOpenOriginal: () -> Unit,
    onOpenModel: (String) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        WpAsyncImage(
            url = listing.imageUrl,
            contentDescription = listing.title,
            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
        )
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = listing.priceLabel,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
            )
            Text(
                text = listing.title,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(top = 6.dp),
            )
            Row(
                modifier = Modifier.padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                listing.source?.takeIf { it.isNotBlank() }?.let { SourceChip(it) }
                listing.postedRelative?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

            InfoRow("Brand", listing.brandName)
            InfoRow("Model", listing.modelName)
            InfoRow("Seller", listing.seller)

            if (!listing.modelSlug.isNullOrBlank()) {
                OutlinedCard(
                    onClick = { onOpenModel(listing.modelSlug) },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text("View market data for this model", style = MaterialTheme.typography.labelLarge)
                        Icon(
                            Icons.Outlined.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }

            Button(
                onClick = onOpenOriginal,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
                enabled = !listing.originalUrl.isNullOrBlank(),
                shape = RoundedCornerShape(14.dp),
            ) {
                Icon(
                    Icons.Outlined.OpenInNew,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Text("  View original post")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
