package ca.pebble.watchpatrol.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import ca.pebble.watchpatrol.data.fx.FxConverter
import ca.pebble.watchpatrol.data.paging.WebListingsPagingSource
import ca.pebble.watchpatrol.data.remote.WatchReconWeb
import ca.pebble.watchpatrol.data.scrape.WatchReconScraper
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
 * Listings come from WatchRecon's public pages — a keyless aggregator of
 * Reddit + the major watch forums. An in-memory cache backs the detail screen.
 */
class ListingsRepository(
    private val web: WatchReconWeb,
    private val fx: FxConverter,
) {

    private val cache = ConcurrentHashMap<Long, Listing>()

    fun listings(query: ListingQuery): Flow<PagingData<Listing>> =
        Pager(
            config = PagingConfig(pageSize = 50, initialLoadSize = 50, enablePlaceholders = false),
        ) {
            WebListingsPagingSource { page ->
                // WatchRecon renders ~50 recent listings per page with no ?page param.
                if (page > 1) {
                    emptyList()
                } else {
                    val html = web.page(query.q?.takeIf { it.isNotBlank() })
                    val parsed = WatchReconScraper.parse(html)
                    fx.toCadAll(parsed).also { list ->
                        list.forEach { cache[it.id] = it }
                    }
                }
            }
        }.flow

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
