package ca.pebble.watchpatrol.di

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import ca.pebble.watchpatrol.WatchPatrolApp

/** Builds a ViewModel with access to the app's dependency graph, no DI framework. */
@Composable
inline fun <reified VM : ViewModel> wpViewModel(
    noinline create: (AppContainer) -> VM,
): VM {
    val container = (LocalContext.current.applicationContext as WatchPatrolApp).container
    return viewModel(factory = viewModelFactory { initializer { create(container) } })
}
