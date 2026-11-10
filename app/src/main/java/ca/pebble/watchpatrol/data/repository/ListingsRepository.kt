package ca.pebble.watchpatrol.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import ca.pebble.watchpatrol.data.paging.CursorPagingSource
import ca.pebble.watchpatrol.data.remote.WatchPatrolApi
import ca.pebble.watchpatrol.domain.model.Listing
import ca.pebble.watchpatrol.domain.model.toDomain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class ListingQuery(
    val q: String? = null,
    val brand: String? = null,
    val model: String? = null,
    val source: String? = null,
    val minPrice: Int? = null,
    val maxPrice: Int? = null,
    val ordering: String? = "-posted_at",
)

class ListingsRepository(private val api: WatchPatrolApi) {

    fun listings(query: ListingQuery): Flow<PagingData<Listing>> =
        Pager(
            config = PagingConfig(pageSize = 20, initialLoadSize = 20, enablePlaceholders = false),
        ) {
            CursorPagingSource(
                firstPage = {
                    api.getListings(
                        q = query.q,
                        brand = query.brand,
                        model = query.model,
                        source = query.source,
                        minPrice = query.minPrice,
                        maxPrice = query.maxPrice,
                        ordering = query.ordering,
                    )
                },
                nextPage = { url -> api.getListingsByUrl(url) },
                map = { it.toDomain() },
            )
        }.flow

    suspend fun listing(id: Long): Listing = api.getListing(id).toDomain()
}
