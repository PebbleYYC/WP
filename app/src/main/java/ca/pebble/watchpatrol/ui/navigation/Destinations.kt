package ca.pebble.watchpatrol.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Star
import androidx.compose.ui.graphics.vector.ImageVector

object Routes {
    const val DISCOVER = "discover"
    const val BROWSE = "browse"
    const val IDENTIFY = "identify"
    const val STARRED = "starred"
    const val SEARCH = "search"

    const val LISTING = "listing/{id}"
    const val BRAND = "brand/{slug}"
    const val MODEL = "model/{slug}"

    fun listing(id: Long) = "listing/$id"
    fun brand(slug: String) = "brand/$slug"
    fun model(slug: String) = "model/$slug"
}

data class BottomItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

val bottomItems = listOf(
    BottomItem(Routes.DISCOVER, "Discover", Icons.Outlined.Explore),
    BottomItem(Routes.BROWSE, "Browse", Icons.Outlined.Category),
    BottomItem(Routes.IDENTIFY, "Identify", Icons.Outlined.CameraAlt),
    BottomItem(Routes.STARRED, "Starred", Icons.Outlined.Star),
)
