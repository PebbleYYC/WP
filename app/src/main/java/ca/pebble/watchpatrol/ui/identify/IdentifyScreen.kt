package ca.pebble.watchpatrol.ui.identify

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAPhoto
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ca.pebble.watchpatrol.di.wpViewModel
import ca.pebble.watchpatrol.domain.model.IdentifyMatch
import ca.pebble.watchpatrol.ui.components.ErrorState
import ca.pebble.watchpatrol.ui.components.LoadingState
import ca.pebble.watchpatrol.ui.components.WpAsyncImage
import ca.pebble.watchpatrol.util.Async
import kotlin.math.roundToInt

@Composable
fun IdentifyScreen(
    onOpenModel: (String) -> Unit,
) {
    val vm = wpViewModel { IdentifyViewModel(it) }
    val state by vm.state.collectAsStateWithLifecycle()
    val selectedImage by vm.selectedImage.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia(),
    ) { uri -> if (uri != null) vm.onImagePicked(context, uri) }

    fun pick() = picker.launch(
        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
    )

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Identify a watch",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
        )
        Text(
            text = "Snap or pick a photo and we'll find the closest models.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        )

        when (val s = state) {
            null -> IdentifyEmpty(onPick = ::pick)
            Async.Loading -> Column(Modifier.fillMaxSize()) {
                selectedImage?.let {
                    WpAsyncImage(
                        url = it.toString(),
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(16.dp).clip(RoundedCornerShape(16.dp)),
                    )
                }
                LoadingState()
            }
            is Async.Error -> ErrorState(s.message, onRetry = ::pick)
            is Async.Success -> IdentifyResults(
                matches = s.data,
                imageUrl = selectedImage?.toString(),
                onOpenModel = onOpenModel,
                onReset = ::pick,
            )
        }
    }
}

@Composable
private fun IdentifyEmpty(onPick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Outlined.AddAPhoto,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Text(
            "Point it at a dial",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            "Works best with a clear, straight-on shot of the watch face.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        Button(onClick = onPick, modifier = Modifier.padding(top = 20.dp)) {
            Icon(Icons.Outlined.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
            Text("  Choose a photo")
        }
    }
}

@Composable
private fun IdentifyResults(
    matches: List<IdentifyMatch>,
    imageUrl: String?,
    onOpenModel: (String) -> Unit,
    onReset: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        if (imageUrl != null) {
            item("photo") {
                WpAsyncImage(
                    url = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().aspectRatio(16f / 9f).clip(RoundedCornerShape(16.dp)),
                )
            }
        }
        item("header") {
            Text(
                if (matches.isEmpty()) "No confident matches" else "Closest matches",
                style = MaterialTheme.typography.titleMedium,
            )
        }
        items(matches.size) { index ->
            MatchCard(match = matches[index], onClick = {
                matches[index].modelSlug?.let(onOpenModel)
            })
        }
        item("reset") {
            OutlinedButton(onClick = onReset, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text("Try another photo")
            }
        }
    }
}

@Composable
private fun MatchCard(match: IdentifyMatch, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            WpAsyncImage(
                url = match.imageUrl,
                contentDescription = match.modelName,
                modifier = Modifier.size(72.dp).clip(RoundedCornerShape(12.dp)),
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                match.brandName?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    match.modelName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    match.confidence?.let {
                        val pct = (it.coerceIn(0.0, 1.0) * 100).roundToInt()
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp)),
                        ) {
                            Text(
                                "$pct% match",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                    if (match.priceLabel != "—") {
                        Text(
                            match.priceLabel,
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.secondary,
                        )
                    }
                }
            }
        }
    }
}
