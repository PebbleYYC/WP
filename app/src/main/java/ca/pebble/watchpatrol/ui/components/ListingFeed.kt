package ca.pebble.watchpatrol.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey
import ca.pebble.watchpatrol.domain.model.Listing

@Composable
fun ListingFeed(
    items: LazyPagingItems<Listing>,
    starredIds: Set<Long>,
    onOpenListing: (Long) -> Unit,
    onToggleStar: (Listing) -> Unit,
    modifier: Modifier = Modifier,
    emptyTitle: String = "Nothing here yet",
    emptySubtitle: String = "Try a different search, or check back soon.",
    header: (@Composable () -> Unit)? = null,
) {
    val refresh = items.loadState.refresh
    when {
        refresh is LoadState.Loading && items.itemCount == 0 -> LoadingState(modifier)
        refresh is LoadState.Error && items.itemCount == 0 ->
            ErrorState(
                message = refresh.error.message ?: "Network error",
                onRetry = { items.retry() },
                modifier = modifier,
            )
        items.itemCount == 0 && refresh is LoadState.NotLoading ->
            EmptyState(
                icon = Icons.Outlined.SearchOff,
                title = emptyTitle,
                subtitle = emptySubtitle,
                modifier = modifier,
            )
        else -> LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            header?.let { item("header") { it() } }
            items(count = items.itemCount, key = items.itemKey { it.id }) { index ->
                items[index]?.let { listing ->
                    ListingCard(
                        listing = listing,
                        isStarred = listing.id in starredIds,
                        onClick = { onOpenListing(listing.id) },
                        onToggleStar = { onToggleStar(listing) },
                    )
                }
            }
            when (val append = items.loadState.append) {
                is LoadState.Loading -> item("append-loading") { AppendLoading() }
                is LoadState.Error -> item("append-error") { AppendError { items.retry() } }
                else -> Unit
            }
        }
    }
}

@Composable
private fun AppendLoading() {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(strokeWidth = 2.dp)
    }
}

@Composable
private fun AppendError(onRetry: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        contentAlignment = Alignment.Center,
    ) {
        TextButton(onClick = onRetry) { Text("Couldn't load more — retry") }
    }
}
