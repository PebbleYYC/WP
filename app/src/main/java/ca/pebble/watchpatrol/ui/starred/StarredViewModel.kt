package ca.pebble.watchpatrol.ui.starred

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.pebble.watchpatrol.data.local.StarredStore
import ca.pebble.watchpatrol.di.AppContainer
import ca.pebble.watchpatrol.domain.model.Listing
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StarredViewModel(
    private val starredStore: StarredStore,
) : ViewModel() {

    constructor(container: AppContainer) : this(container.starredStore)

    val starred = starredStore.starred
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun toggleStar(listing: Listing) {
        viewModelScope.launch { starredStore.toggle(listing) }
    }
}
