package ca.pebble.watchpatrol.data.repository

import ca.pebble.watchpatrol.data.remote.WatchPatrolApi
import ca.pebble.watchpatrol.data.remote.dto.BulkHistoryRequest
import ca.pebble.watchpatrol.domain.model.PricePoint
import ca.pebble.watchpatrol.domain.model.PriceStats
import ca.pebble.watchpatrol.domain.model.toDomain
import ca.pebble.watchpatrol.domain.model.toPoints

class PricingRepository(private val api: WatchPatrolApi) {

    suspend fun stats(modelSlug: String, currency: String = "USD"): PriceStats =
        api.getPricing(modelSlug, currency).toDomain()

    suspend fun history(
        modelSlug: String,
        currency: String = "USD",
        granularity: String = "monthly",
    ): List<PricePoint> =
        api.getBulkHistory(
            BulkHistoryRequest(models = listOf(modelSlug), granularity = granularity, currency = currency),
        ).firstOrNull()?.toPoints().orEmpty()
}
