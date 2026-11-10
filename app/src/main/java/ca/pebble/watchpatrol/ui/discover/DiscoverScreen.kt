package ca.pebble.watchpatrol.ui.discover

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
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
    val creds by vm.redditCreds.collectAsStateWithLifecycle()

    var showKeyDialog by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "WatchPatrol", style = MaterialTheme.typography.headlineMedium)
                IconButton(onClick = { showKeyDialog = true }) {
                    Icon(Icons.Outlined.Key, contentDescription = "Reddit API key")
                }
            }
            Text(
                text = "Fresh listings from r/Watchexchange",
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

    if (showKeyDialog) {
        RedditKeyDialog(
            initialId = creds?.id.orEmpty(),
            initialSecret = creds?.secret.orEmpty(),
            onSave = { id, secret ->
                vm.saveRedditCreds(id, secret)
                showKeyDialog = false
                items.refresh()
            },
            onDismiss = { showKeyDialog = false },
        )
    }
}

@Composable
private fun RedditKeyDialog(
    initialId: String,
    initialSecret: String,
    onSave: (String, String) -> Unit,
    onDismiss: () -> Unit,
) {
    var id by remember { mutableStateOf(initialId) }
    var secret by remember { mutableStateOf(initialSecret) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reddit API key") },
        text = {
            Column {
                Text(
                    "From reddit.com/prefs/apps — paste your app's client ID and secret. " +
                        "Use a \"web app\" type.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                OutlinedTextField(
                    value = id,
                    onValueChange = { id = it.trim() },
                    label = { Text("Client ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                OutlinedTextField(
                    value = secret,
                    onValueChange = { secret = it.trim() },
                    label = { Text("Client secret") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(id, secret) },
                enabled = id.isNotBlank() && secret.isNotBlank(),
            ) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
