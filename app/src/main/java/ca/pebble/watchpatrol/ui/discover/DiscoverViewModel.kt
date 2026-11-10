package ca.pebble.watchpatrol.ui.discover

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import ca.pebble.watchpatrol.data.local.StarredStore
import ca.pebble.watchpatrol.data.repository.ListingQuery
import ca.pebble.watchpatrol.data.repository.ListingsRepository
import ca.pebble.watchpatrol.di.AppContainer
import ca.pebble.watchpatrol.domain.model.Listing
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiscoverViewModel(
    listingsRepository: ListingsRepository,
    private val starredStore: StarredStore,
) : ViewModel() {

    constructor(container: AppContainer) : this(
        container.listingsRepository,
        container.starredStore,
    )

    val listings = listingsRepository
        .listings(ListingQuery(ordering = "-posted_at"))
        .cachedIn(viewModelScope)

    val starredIds = starredStore.starredIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun toggleStar(listing: Listing) {
        viewModelScope.launch { starredStore.toggle(listing) }
    }
}
