package ca.pebble.watchpatrol.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import ca.pebble.watchpatrol.data.local.StarredStore
import ca.pebble.watchpatrol.data.repository.ListingQuery
import ca.pebble.watchpatrol.data.repository.ListingsRepository
import ca.pebble.watchpatrol.di.AppContainer
import ca.pebble.watchpatrol.domain.model.Listing
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SearchViewModel(
    private val listingsRepository: ListingsRepository,
    private val starredStore: StarredStore,
) : ViewModel() {

    constructor(container: AppContainer) : this(
        container.listingsRepository,
        container.starredStore,
    )

    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val results = _query
        .debounce(350)
        .map { it.trim() }
        .distinctUntilChanged()
        .flatMapLatest { q ->
            if (q.length < 2) {
                flowOf(PagingData.empty())
            } else {
                listingsRepository.listings(ListingQuery(q = q, ordering = null))
            }
        }
        .cachedIn(viewModelScope)

    val starredIds = starredStore.starredIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    fun onQueryChange(value: String) { _query.value = value }

    fun clear() { _query.value = "" }

    fun toggleStar(listing: Listing) {
        viewModelScope.launch { starredStore.toggle(listing) }
    }
}
