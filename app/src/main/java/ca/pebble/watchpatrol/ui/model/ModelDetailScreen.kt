package ca.pebble.watchpatrol.ui.model

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ca.pebble.watchpatrol.di.wpViewModel
import ca.pebble.watchpatrol.domain.model.PriceStats
import ca.pebble.watchpatrol.domain.model.WatchModel
import ca.pebble.watchpatrol.ui.components.ErrorState
import ca.pebble.watchpatrol.ui.components.ListingCard
import ca.pebble.watchpatrol.ui.components.LoadingState
import ca.pebble.watchpatrol.ui.components.PriceHistogram
import ca.pebble.watchpatrol.ui.components.PriceHistoryChart
import ca.pebble.watchpatrol.ui.components.Tag
import ca.pebble.watchpatrol.ui.components.WpAsyncImage
import ca.pebble.watchpatrol.ui.components.WpTopBar
import ca.pebble.watchpatrol.util.Async
import ca.pebble.watchpatrol.util.prettifySlug

@Composable
fun ModelDetailScreen(
    modelSlug: String,
    onBack: () -> Unit,
) {
    val vm = wpViewModel {
        ModelDetailViewModel(
            it.catalogRepository,
            it.pricingRepository,
            it.listingsRepository,
            it.starredStore,
            modelSlug,
        )
    }
    val state by vm.state.collectAsStateWithLifecycle()
    val starredIds by vm.starredIds.collectAsStateWithLifecycle()
    val listings = vm.listings.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        val title = (state as? Async.Success)?.data?.model?.name ?: prettifySlug(modelSlug)
        WpTopBar(title = title, onBack = onBack)

        when (val s = state) {
            Async.Loading -> LoadingState()
            is Async.Error -> ErrorState(s.message, onRetry = { vm.load() })
            is Async.Success -> {
                val market = s.data
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    item("hero") { ModelHero(market.model) }

                    market.stats?.let { stats ->
                        item("market") { MarketCard(stats) }
                        if (stats.histogram.isNotEmpty()) {
                            item("hist") {
                                SectionCard("Price distribution") {
                                    PriceHistogram(stats.histogram)
                                }
                            }
                        }
                    }

                    if (market.history.size >= 2) {
                        item("history") {
                            SectionCard("Price history") {
                                PriceHistoryChart(market.history)
                                Text(
                                    text = "${market.history.first().label} – ${market.history.last().label}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }
                    }

                    item("listings-header") {
                        Text("Listings", style = MaterialTheme.typography.titleMedium)
                    }

                    items(count = listings.itemCount, key = listings.itemKey { it.id }) { index ->
                        listings[index]?.let { listing ->
                            ListingCard(
                                listing = listing,
                                isStarred = listing.id in starredIds,
                                onClick = { /* already viewing this model's listings */ },
                                onToggleStar = { vm.toggleStar(listing) },
                            )
                        }
                    }

                    if (listings.itemCount == 0 && listings.loadState.refresh is LoadState.NotLoading) {
                        item("no-listings") {
                            Text(
                                "No active listings for this model right now.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelHero(model: WatchModel) {
    Column {
        WpAsyncImage(
            url = model.imageUrl,
            contentDescription = model.name,
            modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f).clip(RoundedCornerShape(16.dp)),
        )
        Text(
            text = model.name,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        val sub = buildList {
            model.brandName?.let { add(it) }
            model.caseSizeMm?.let { add("${it.toInt()}mm") }
        }.joinToString(" · ")
        if (sub.isNotBlank()) {
            Text(
                text = sub,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        if (model.categoryTags.isNotEmpty()) {
            Row(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                model.categoryTags.take(4).forEach { Tag(it) }
            }
        }
    }
}

@Composable
private fun MarketCard(stats: PriceStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Median price",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stats.medianLabel,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 2.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatTile("25th", ca.pebble.watchpatrol.util.Formatters.priceCompact(stats.p25, stats.currency), Modifier.weight(1f))
                StatTile("75th", ca.pebble.watchpatrol.util.Formatters.priceCompact(stats.p75, stats.currency), Modifier.weight(1f))
                StatTile("Samples", stats.count?.toString() ?: "—", Modifier.weight(1f))
            }
            if (stats.rangeLabel != "—") {
                Text(
                    text = "Range ${stats.rangeLabel}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 14.dp),
                )
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(top = 12.dp)) { content() }
        }
    }
}
