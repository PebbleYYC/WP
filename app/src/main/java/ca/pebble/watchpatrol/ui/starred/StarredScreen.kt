package ca.pebble.watchpatrol.ui.starred

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.pebble.watchpatrol.di.wpViewModel
import ca.pebble.watchpatrol.ui.components.EmptyState
import ca.pebble.watchpatrol.ui.components.ListingCard

@Composable
fun StarredScreen(
    onOpenListing: (Long) -> Unit,
) {
    val vm = wpViewModel { StarredViewModel(it) }
    val starred by vm.starred.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Starred",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        )
        if (starred.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.StarOutline,
                title = "No starred listings",
                subtitle = "Tap the star on any listing to save it here for later.",
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                items(starred.size, key = { starred[it].id }) { index ->
                    val listing = starred[index]
                    ListingCard(
                        listing = listing,
                        isStarred = true,
                        onClick = { onOpenListing(listing.id) },
                        onToggleStar = { vm.toggleStar(listing) },
                    )
                }
            }
        }
    }
}
