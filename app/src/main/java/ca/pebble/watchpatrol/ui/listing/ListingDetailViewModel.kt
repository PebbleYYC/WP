package ca.pebble.watchpatrol.ui.listing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.pebble.watchpatrol.data.local.StarredStore
import ca.pebble.watchpatrol.data.repository.ListingsRepository
import ca.pebble.watchpatrol.di.AppContainer
import ca.pebble.watchpatrol.domain.model.Listing
import ca.pebble.watchpatrol.util.Async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ListingDetailViewModel(
    private val listingsRepository: ListingsRepository,
    private val starredStore: StarredStore,
) : ViewModel() {

    constructor(container: AppContainer) : this(
        container.listingsRepository,
        container.starredStore,
    )

    private val _state = MutableStateFlow<Async<Listing>>(Async.Loading)
    val state = _state.asStateFlow()

    val starredIds = starredStore.starredIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun load(id: Long) {
        _state.value = Async.Loading
        viewModelScope.launch {
            _state.value = try {
                Async.Success(listingsRepository.listing(id))
            } catch (e: Exception) {
                Async.Error(e.message ?: "Couldn't load this listing")
            }
        }
    }

    fun toggleStar(listing: Listing) {
        viewModelScope.launch { starredStore.toggle(listing) }
    }
}
