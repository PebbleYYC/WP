package ca.pebble.watchpatrol.ui.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
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
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import ca.pebble.watchpatrol.di.wpViewModel
import ca.pebble.watchpatrol.domain.model.Brand
import ca.pebble.watchpatrol.ui.components.EmptyState
import ca.pebble.watchpatrol.ui.components.ErrorState
import ca.pebble.watchpatrol.ui.components.LoadingState
import ca.pebble.watchpatrol.ui.components.WpAsyncImage

@Composable
fun BrowseScreen(
    onOpenBrand: (String) -> Unit,
    onOpenSearch: () -> Unit,
) {
    val vm = wpViewModel { BrowseViewModel(it) }
    val brands = vm.brands.collectAsLazyPagingItems()

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Browse brands",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp),
        )
        BrandGrid(brands = brands, onOpenBrand = onOpenBrand)
    }
}

@Composable
private fun BrandGrid(
    brands: LazyPagingItems<Brand>,
    onOpenBrand: (String) -> Unit,
) {
    val refresh = brands.loadState.refresh
    when {
        refresh is LoadState.Loading && brands.itemCount == 0 -> LoadingState()
        refresh is LoadState.Error && brands.itemCount == 0 ->
            ErrorState(refresh.error.message ?: "Network error", onRetry = { brands.retry() })
        brands.itemCount == 0 && refresh is LoadState.NotLoading ->
            EmptyState(Icons.Outlined.Category, "No brands", "The catalog came back empty.")
        else -> LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(count = brands.itemCount, key = brands.itemKey { it.slug }) { index ->
                brands[index]?.let { brand ->
                    BrandCard(brand = brand, onClick = { onOpenBrand(brand.slug) })
                }
            }
            if (brands.loadState.append is LoadState.Loading) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(strokeWidth = 2.dp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandCard(brand: Brand, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().aspectRatio(1.6f)) {
                if (!brand.imageUrl.isNullOrBlank()) {
                    WpAsyncImage(brand.imageUrl, brand.name, modifier = Modifier.fillMaxSize())
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(0.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = brand.name.take(1).uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = brand.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                brand.listingCount?.let {
                    Text(
                        text = "$it listings",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }
        }
    }
}
