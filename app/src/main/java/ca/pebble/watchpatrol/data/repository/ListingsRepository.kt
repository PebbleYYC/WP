package ca.pebble.watchpatrol.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ca.pebble.watchpatrol.data.paging.WebListingsPagingSource
import ca.pebble.watchpatrol.data.remote.WatchPatrolWeb
import ca.pebble.watchpatrol.data.scrape.ListingScraper
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
 * Listings come from WatchPatrol's public pages (scraped), because the JSON
 * listings API is gated behind a partner key. A small in-memory cache lets the
 * detail screen show a tapped listing without re-fetching.
 */
class ListingsRepository(private val web: WatchPatrolWeb) {

    private val cache = ConcurrentHashMap<Long, Listing>()

    fun listings(query: ListingQuery): Flow<PagingData<Listing>> =
        Pager(
            config = PagingConfig(pageSize = 55, initialLoadSize = 55, enablePlaceholders = false),
        ) {
            WebListingsPagingSource { page ->
                val html = web.page(query = query.q?.takeIf { it.isNotBlank() }, page = page)
                ListingScraper.parse(html).also { list ->
                    list.forEach { cache[it.id] = it }
                }
            }
        }.flow

    /** Returns the tapped listing from cache, or a stub that still opens on WatchPatrol. */
    suspend fun listing(id: Long): Listing = cache[id] ?: Listing(
        id = id,
        title = "View on WatchPatrol",
        price = null,
        currency = null,
        priceLabel = "—",
        source = null,
        seller = null,
        imageUrl = null,
        originalUrl = "https://www.watchpatrol.net/listing/$id/",
        postedRelative = null,
        brandName = null,
        modelName = null,
        modelSlug = null,
    )
}
