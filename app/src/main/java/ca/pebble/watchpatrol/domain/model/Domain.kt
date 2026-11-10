package ca.pebble.watchpatrol.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Listing(
    val id: Long,
    val title: String,
    val price: Double?,
    val currency: String?,
    val priceLabel: String,
    val source: String?,
    val seller: String?,
    val imageUrl: String?,
    val originalUrl: String?,
    val postedRelative: String?,
    val brandName: String?,
    val modelName: String?,
    val modelSlug: String?,
)

data class Brand(
    val slug: String,
    val name: String,
    val listingCount: Int?,
    val imageUrl: String?,
)

data class WatchModel(
    val slug: String,
    val name: String,
    val brandName: String?,
    val brandSlug: String?,
    val imageUrl: String?,
    val caseSizeMm: Double?,
    val categoryTags: List<String>,
    val priceLabel: String,
    val listingCount: Int?,
)

data class HistogramBucket(
    val lower: Double?,
    val upper: Double?,
    val count: Int,
)

data class PriceStats(
    val median: Double?,
    val p25: Double?,
    val p75: Double?,
    val min: Double?,
    val max: Double?,
    val count: Int?,
    val currency: String?,
    val medianLabel: String,
    val rangeLabel: String,
    val histogram: List<HistogramBucket>,
)

data class PricePoint(
    val label: String,
    val value: Double,
)

data class IdentifyMatch(
    val modelSlug: String?,
    val modelName: String,
    val brandName: String?,
    val confidence: Double?,
    val imageUrl: String?,
    val priceLabel: String,
)
