package ca.pebble.watchpatrol.ui.navigation

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ca.pebble.watchpatrol.ui.browse.BrandModelsScreen
import ca.pebble.watchpatrol.ui.browse.BrowseScreen
import ca.pebble.watchpatrol.ui.discover.DiscoverScreen
import ca.pebble.watchpatrol.ui.identify.IdentifyScreen
import ca.pebble.watchpatrol.ui.listing.ListingDetailScreen
import ca.pebble.watchpatrol.ui.model.ModelDetailScreen
import ca.pebble.watchpatrol.ui.search.SearchScreen
import ca.pebble.watchpatrol.ui.starred.StarredScreen

@Composable
fun WatchPatrolRoot() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevelRoutes = bottomItems.map { it.route }.toSet()

    Scaffold(
        bottomBar = {
            if (currentRoute in topLevelRoutes) {
                NavigationBar {
                    bottomItems.forEach { item ->
                        NavigationBarItem(
                            selected = currentRoute == item.route,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.DISCOVER,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.DISCOVER) {
                DiscoverScreen(
                    onOpenListing = { navController.navigate(Routes.listing(it)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                )
            }
            composable(Routes.BROWSE) {
                BrowseScreen(
                    onOpenBrand = { navController.navigate(Routes.brand(it)) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                )
            }
            composable(Routes.IDENTIFY) {
                IdentifyScreen(
                    onOpenModel = { navController.navigate(Routes.model(it)) },
                )
            }
            composable(Routes.STARRED) {
                StarredScreen(
                    onOpenListing = { navController.navigate(Routes.listing(it)) },
                )
            }
            composable(Routes.SEARCH) {
                SearchScreen(
                    onOpenListing = { navController.navigate(Routes.listing(it)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.LISTING,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                ListingDetailScreen(
                    listingId = entry.arguments?.getLong("id") ?: 0L,
                    onBack = { navController.popBackStack() },
                    onOpenModel = { navController.navigate(Routes.model(it)) },
                )
            }
            composable(
                route = Routes.BRAND,
                arguments = listOf(navArgument("slug") { type = NavType.StringType }),
            ) { entry ->
                BrandModelsScreen(
                    brandSlug = entry.arguments?.getString("slug").orEmpty(),
                    onOpenModel = { navController.navigate(Routes.model(it)) },
                    onBack = { navController.popBackStack() },
                )
            }
            composable(
                route = Routes.MODEL,
                arguments = listOf(navArgument("slug") { type = NavType.StringType }),
            ) { entry ->
                ModelDetailScreen(
                    modelSlug = entry.arguments?.getString("slug").orEmpty(),
                    onBack = { navController.popBackStack() },
                )
            }
        }
    }
}
