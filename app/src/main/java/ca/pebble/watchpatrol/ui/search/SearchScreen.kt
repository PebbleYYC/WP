package ca.pebble.watchpatrol.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import ca.pebble.watchpatrol.di.wpViewModel
import ca.pebble.watchpatrol.ui.components.EmptyState
import ca.pebble.watchpatrol.ui.components.ListingFeed
import ca.pebble.watchpatrol.ui.components.SearchField

@Composable
fun SearchScreen(
    onOpenListing: (Long) -> Unit,
    onBack: () -> Unit,
) {
    val vm = wpViewModel { SearchViewModel(it) }
    val query by vm.query.collectAsStateWithLifecycle()
    val items = vm.results.collectAsLazyPagingItems()
    val starredIds by vm.starredIds.collectAsStateWithLifecycle()

    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 4.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
            SearchField(
                value = query,
                onValueChange = vm::onQueryChange,
                onClear = vm::clear,
                onSubmit = { keyboard?.hide() },
                modifier = Modifier.focusRequester(focusRequester),
            )
        }
        if (query.isBlank()) {
            EmptyState(
                icon = Icons.Outlined.Search,
                title = "Search WatchPatrol",
                subtitle = "Find watches by brand, model or reference across every source.",
            )
        } else {
            ListingFeed(
                items = items,
                starredIds = starredIds,
                onOpenListing = onOpenListing,
                onToggleStar = vm::toggleStar,
                emptyTitle = "No matches",
                emptySubtitle = "Nothing found for \"$query\". Try another term.",
            )
        }
    }
}
