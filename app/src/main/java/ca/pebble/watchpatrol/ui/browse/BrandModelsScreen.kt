package ca.pebble.watchpatrol.ui.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Watch
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ca.pebble.watchpatrol.di.wpViewModel
import ca.pebble.watchpatrol.domain.model.WatchModel
import ca.pebble.watchpatrol.ui.components.EmptyState
import ca.pebble.watchpatrol.ui.components.ErrorState
import ca.pebble.watchpatrol.ui.components.LoadingState
import ca.pebble.watchpatrol.ui.components.Tag
import ca.pebble.watchpatrol.ui.components.WpAsyncImage
import ca.pebble.watchpatrol.ui.components.WpTopBar
import ca.pebble.watchpatrol.util.prettifySlug

@Composable
fun BrandModelsScreen(
    brandSlug: String,
    onOpenModel: (String) -> Unit,
    onBack: () -> Unit,
) {
    val vm = wpViewModel { BrandModelsViewModel(it.catalogRepository, brandSlug) }
    val models = vm.models.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        WpTopBar(title = prettifySlug(brandSlug), onBack = onBack)
        val refresh = models.loadState.refresh
        when {
            refresh is LoadState.Loading && models.itemCount == 0 -> LoadingState()
            refresh is LoadState.Error && models.itemCount == 0 ->
                ErrorState(refresh.error.message ?: "Network error", onRetry = { models.retry() })
            models.itemCount == 0 && refresh is LoadState.NotLoading ->
                EmptyState(Icons.Outlined.Watch, "No models", "No models listed for this brand yet.")
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(count = models.itemCount, key = models.itemKey { it.slug }) { index ->
                    models[index]?.let { model ->
                        ModelRow(model = model, onClick = { onOpenModel(model.slug) })
                    }
                }
                if (models.loadState.append is LoadState.Loading) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModelRow(model: WatchModel, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            WpAsyncImage(
                url = model.imageUrl,
                contentDescription = model.name,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                val subtitle = buildList {
                    model.brandName?.let { add(it) }
                    model.caseSizeMm?.let { add("${it.toInt()}mm") }
                }.joinToString(" · ")
                if (subtitle.isNotBlank()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
                if (model.priceLabel != "—") {
                    Text(
                        text = "Median ${model.priceLabel}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
                if (model.categoryTags.isNotEmpty()) {
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        model.categoryTags.take(2).forEach { Tag(it) }
                    }
                }
            }
        }
    }
}
