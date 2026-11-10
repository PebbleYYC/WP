package ca.pebble.watchpatrol.domain.model

import ca.pebble.watchpatrol.data.remote.dto.BrandDto
import ca.pebble.watchpatrol.data.remote.dto.IdentifyMatchDto
import ca.pebble.watchpatrol.data.remote.dto.ListingDto
import ca.pebble.watchpatrol.data.remote.dto.ModelDto
import ca.pebble.watchpatrol.data.remote.dto.PriceHistoryDto
import ca.pebble.watchpatrol.data.remote.dto.PricingDto
import ca.pebble.watchpatrol.util.Formatters

fun ListingDto.toDomain(): Listing = Listing(
    id = id ?: 0L,
    title = title?.takeIf { it.isNotBlank() } ?: "Untitled listing",
    price = price,
    currency = currency,
    priceLabel = Formatters.price(price, currency),
    source = source,
    seller = seller,
    imageUrl = image ?: thumbnail,
    originalUrl = url,
    postedRelative = Formatters.relativeTime(postedAt ?: createdAt),
    brandName = brand,
    modelName = model,
    modelSlug = modelSlug,
)

fun BrandDto.toDomain(): Brand = Brand(
    slug = slug.orEmpty(),
    name = name?.takeIf { it.isNotBlank() } ?: slug.orEmpty(),
    listingCount = listingCount,
    imageUrl = image,
)

fun ModelDto.toDomain(): WatchModel = WatchModel(
    slug = slug.orEmpty(),
    name = name?.takeIf { it.isNotBlank() } ?: slug.orEmpty(),
    brandName = brand,
    brandSlug = brandSlug,
    imageUrl = image,
    caseSizeMm = caseSizeMm,
    categoryTags = categoryTags,
    priceLabel = Formatters.price(medianPrice, currency),
    listingCount = listingCount,
)

fun PricingDto.toDomain(): PriceStats {
    val range = if (min != null && max != null) {
        "${Formatters.priceCompact(min, currency)} – ${Formatters.priceCompact(max, currency)}"
    } else "—"
    return PriceStats(
        median = median,
        p25 = p25,
        p75 = p75,
        min = min,
        max = max,
        count = count,
        currency = currency,
        medianLabel = Formatters.price(median, currency),
        rangeLabel = range,
        histogram = histogram.map {
            HistogramBucket(lower = it.lower, upper = it.upper, count = it.count)
        },
    )
}

fun PriceHistoryDto.toPoints(): List<PricePoint> = points.mapNotNull { point ->
    val value = point.median ?: return@mapNotNull null
    val label = point.date?.take(7) ?: ""
    PricePoint(label = label, value = value)
}

fun IdentifyMatchDto.toDomain(): IdentifyMatch = IdentifyMatch(
    modelSlug = modelSlug,
    modelName = model?.takeIf { it.isNotBlank() } ?: modelSlug.orEmpty(),
    brandName = brand,
    confidence = confidence,
    imageUrl = image,
    priceLabel = Formatters.price(medianPrice, currency),
)
