package ca.pebble.watchpatrol.ui.discover

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import ca.pebble.watchpatrol.di.wpViewModel
import ca.pebble.watchpatrol.ui.components.ClickableSearchBar
import ca.pebble.watchpatrol.ui.components.ListingFeed

@Composable
fun DiscoverScreen(
    onOpenListing: (Long) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val vm = wpViewModel { DiscoverViewModel(it) }
    val items = vm.listings.collectAsLazyPagingItems()
    val starredIds by vm.starredIds.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
            Text(text = "WatchPatrol", style = MaterialTheme.typography.headlineMedium)
            Text(
                text = "Fresh listings across Reddit & the watch forums",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp, bottom = 12.dp),
            )
            ClickableSearchBar(
                placeholder = "Search watches, brands, refs…",
                onClick = onOpenSearch,
            )
        }
        ListingFeed(
            items = items,
            starredIds = starredIds,
            onOpenListing = onOpenListing,
            onToggleStar = vm::toggleStar,
            emptyTitle = "No listings yet",
            emptySubtitle = "Pull to refresh, or check back shortly.",
        )
    }
}
