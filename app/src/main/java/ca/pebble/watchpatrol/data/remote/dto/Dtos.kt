package ca.pebble.watchpatrol.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/*
 * ─────────────────────────────────────────────────────────────────────────
 *  SINGLE SOURCE OF TRUTH FOR THE WIRE FORMAT.
 *
 *  These DTOs mirror the WatchPatrol REST API as described on
 *  watchpatrol.net/integrations. Field NAMES below are the best-guess
 *  snake_case (DRF-style). If the live OpenAPI schema differs, fix the
 *  @SerialName values here — nothing else in the app needs to change,
 *  because every screen consumes the mapped domain models, not DTOs.
 *
 *  The JSON parser is lenient + ignores unknown keys, so extra fields are
 *  harmless; only the names we read here matter.
 * ─────────────────────────────────────────────────────────────────────────
 */

/** DRF-style cursor-paginated envelope. `next`/`previous` are absolute URLs. */
@Serializable
data class Paginated<T>(
    val results: List<T> = emptyList(),
    val next: String? = null,
    val previous: String? = null,
    val count: Int? = null,
)

/**
 * Parsed by [ListingDtoSerializer], which tolerantly reads whatever key
 * spellings the live API actually uses — string-or-nested brand/model/seller,
 * and several possible image/date/url key names — so the feed populates even
 * before the exact schema is confirmed. These field names feed the mapper.
 */
@Serializable(with = ListingDtoSerializer::class)
data class ListingDto(
    val id: Long? = null,
    val title: String? = null,
    val price: Double? = null,
    val currency: String? = null,
    val source: String? = null,
    val seller: String? = null,
    val url: String? = null,
    val image: String? = null,
    val thumbnail: String? = null,
    val postedAt: String? = null,
    val createdAt: String? = null,
    val brand: String? = null,
    val brandSlug: String? = null,
    val model: String? = null,
    val modelSlug: String? = null,
)

@Serializable
data class BrandDto(
    val slug: String? = null,
    val name: String? = null,
    @SerialName("listing_count")
    val listingCount: Int? = null,
    val image: String? = null,
)

@Serializable
data class ModelDto(
    val slug: String? = null,
    val name: String? = null,
    val brand: String? = null,
    @SerialName("brand_slug")
    val brandSlug: String? = null,
    val image: String? = null,
    @SerialName("case_size_mm")
    @Serializable(FlexibleDoubleSerializer::class)
    val caseSizeMm: Double? = null,
    @SerialName("category_tags")
    val categoryTags: List<String> = emptyList(),
    @SerialName("median_price")
    @Serializable(FlexibleDoubleSerializer::class)
    val medianPrice: Double? = null,
    val currency: String? = null,
    @SerialName("listing_count")
    val listingCount: Int? = null,
)

@Serializable
data class PricingDto(
    @Serializable(FlexibleDoubleSerializer::class)
    val median: Double? = null,
    @SerialName("p25")
    @Serializable(FlexibleDoubleSerializer::class)
    val p25: Double? = null,
    @SerialName("p75")
    @Serializable(FlexibleDoubleSerializer::class)
    val p75: Double? = null,
    @Serializable(FlexibleDoubleSerializer::class)
    val min: Double? = null,
    @Serializable(FlexibleDoubleSerializer::class)
    val max: Double? = null,
    val count: Int? = null,
    val currency: String? = null,
    val histogram: List<HistogramBucketDto> = emptyList(),
)

@Serializable
data class HistogramBucketDto(
    @SerialName("lower")
    @Serializable(FlexibleDoubleSerializer::class)
    val lower: Double? = null,
    @SerialName("upper")
    @Serializable(FlexibleDoubleSerializer::class)
    val upper: Double? = null,
    val count: Int = 0,
)

/** Request body for POST /pricing/bulk-history/. */
@Serializable
data class BulkHistoryRequest(
    val models: List<String>,
    val granularity: String = "monthly",
    val currency: String = "USD",
)

@Serializable
data class PriceHistoryDto(
    val slug: String? = null,
    @SerialName("model_slug")
    val modelSlug: String? = null,
    val points: List<PricePointDto> = emptyList(),
)

@Serializable
data class PricePointDto(
    val date: String? = null,
    @Serializable(FlexibleDoubleSerializer::class)
    val median: Double? = null,
)

@Serializable
data class IdentifyResponseDto(
    val matches: List<IdentifyMatchDto> = emptyList(),
)

@Serializable
data class IdentifyMatchDto(
    @SerialName("model_slug")
    val modelSlug: String? = null,
    val model: String? = null,
    val brand: String? = null,
    @Serializable(FlexibleDoubleSerializer::class)
    val confidence: Double? = null,
    val image: String? = null,
    @SerialName("median_price")
    @Serializable(FlexibleDoubleSerializer::class)
    val medianPrice: Double? = null,
    val currency: String? = null,
)
