package ca.pebble.watchpatrol.ui.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.cachedIn
import ca.pebble.watchpatrol.data.local.StarredStore
import ca.pebble.watchpatrol.data.repository.CatalogRepository
import ca.pebble.watchpatrol.data.repository.ListingQuery
import ca.pebble.watchpatrol.data.repository.ListingsRepository
import ca.pebble.watchpatrol.data.repository.PricingRepository
import ca.pebble.watchpatrol.domain.model.Listing
import ca.pebble.watchpatrol.domain.model.PricePoint
import ca.pebble.watchpatrol.domain.model.PriceStats
import ca.pebble.watchpatrol.domain.model.WatchModel
import ca.pebble.watchpatrol.util.Async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ModelMarket(
    val model: WatchModel,
    val stats: PriceStats?,
    val history: List<PricePoint>,
)

class ModelDetailViewModel(
    private val catalogRepository: CatalogRepository,
    private val pricingRepository: PricingRepository,
    listingsRepository: ListingsRepository,
    private val starredStore: StarredStore,
    private val modelSlug: String,
) : ViewModel() {

    private val _state = MutableStateFlow<Async<ModelMarket>>(Async.Loading)
    val state = _state.asStateFlow()

    val listings = listingsRepository
        .listings(ListingQuery(model = modelSlug, ordering = "-posted_at"))
        .cachedIn(viewModelScope)

    val starredIds = starredStore.starredIds
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    init { load() }

    fun load() {
        _state.value = Async.Loading
        viewModelScope.launch {
            _state.value = try {
                val model = catalogRepository.model(modelSlug)
                val stats = runCatching { pricingRepository.stats(modelSlug) }.getOrNull()
                val history = runCatching { pricingRepository.history(modelSlug) }.getOrDefault(emptyList())
                Async.Success(ModelMarket(model, stats, history))
            } catch (e: Exception) {
                Async.Error(e.message ?: "Couldn't load this model")
            }
        }
    }

    fun toggleStar(listing: Listing) {
        viewModelScope.launch { starredStore.toggle(listing) }
    }
}
