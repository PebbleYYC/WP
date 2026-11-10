package ca.pebble.watchpatrol.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ca.pebble.watchpatrol.data.paging.RedditPagingSource
import ca.pebble.watchpatrol.data.reddit.RedditClient
import ca.pebble.watchpatrol.domain.model.Listing
import kotlinx.coroutines.flow.Flow
import java.util.concurrent.ConcurrentHashMap

data class ListingQuery(
    val q: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val source: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val ordering: String? = null,
)

/**
 * Listings come straight from r/Watchexchange via Reddit's official API.
 * A small in-memory cache lets the detail screen show a tapped listing
 * without re-fetching.
 */
class ListingsRepository(private val reddit: RedditClient) {

    private val cache = ConcurrentHashMap<Long, Listing>()

    fun listings(query: ListingQuery): Flow<PagingData<Listing>> =
        Pager(
            config = PagingConfig(pageSize = 100, initialLoadSize = 100, enablePlaceholders = false),
        ) {
            RedditPagingSource { after ->
                reddit.fetch(query.q?.takeIf { it.isNotBlank() }, after).also { (items, _) ->
                    items.forEach { cache[it.id] = it }
                }
            }
        }.flow

    /** Returns the tapped listing from cache, or a stub that still opens the source link. */
    suspend fun listing(id: Long): Listing = cache[id] ?: Listing(
        id = id,
        title = "Open original listing",
        price = null,
        currency = null,
        priceLabel = "—",
        source = null,
        seller = null,
        imageUrl = null,
        originalUrl = null,
        postedRelative = null,
        brandName = null,
        modelName = null,
        modelSlug = null,
    )
}
