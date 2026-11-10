package ca.pebble.watchpatrol.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

/**
 * Read-only, defensive deserializer for a WatchPatrol listing. The live API's
 * exact field names aren't publicly documented, so rather than hard-code one
 * guess we probe a list of likely key spellings for each value and accept
 * either a plain string or a nested object (e.g. brand as "Rolex" or
 * {"name":"Rolex","slug":"rolex"}). Unknown keys are ignored.
 */
object ListingDtoSerializer : KSerializer<ListingDto> {

    override val descriptor = buildClassSerialDescriptor("ListingDto")

    override fun serialize(encoder: Encoder, value: ListingDto) {
        throw UnsupportedOperationException("ListingDto is read-only")
    }

    override fun deserialize(decoder: Decoder): ListingDto {
        val json = decoder as? JsonDecoder
            ?: throw IllegalStateException("ListingDto can only be read from JSON")
        val o = json.decodeJsonElement().jsonObject
        return ListingDto(
            id = o.longVal("id", "listing_id", "listingId", "pk"),
            title = o.strVal("title", "name", "headline", "listing_title"),
            price = o.dblVal("price", "price_usd", "priceUsd", "amount", "asking_price"),
            currency = o.strVal("currency", "currency_code"),
            source = o.strVal("source", "source_name", "sourceName", "source_detail", "sourceDetail"),
            seller = o.strVal("seller", "seller_name", "sellerName", "seller_username", "username"),
            url = o.strVal("url", "listing_url", "listingUrl", "permalink", "link", "source_url"),
            image = o.strVal(
                "image", "image_url", "imageUrl", "thumbnail", "thumbnail_url",
                "thumb", "img", "photo", "image_full", "picture",
            ),
            thumbnail = null,
            postedAt = o.strVal(
                "posted_at", "postedAt", "created_at", "createdAt",
                "date", "timestamp", "listed_at", "published_at",
            ),
            createdAt = null,
            brand = o.strVal("brand", "brand_name", "brandName"),
            brandSlug = o.strVal("brand_slug", "brandSlug") ?: o.nestedSlug("brand"),
            model = o.strVal("model", "model_name", "modelName"),
            modelSlug = o.strVal("model_slug", "modelSlug") ?: o.nestedSlug("model"),
        )
    }

    /** First present value among [keys] as a string; nested objects yield name/username/title/slug. */
    private fun JsonObject.strVal(vararg keys: String): String? {
        for (k in keys) {
            when (val e = this[k]) {
                is JsonPrimitive -> {
                    val c = e.contentOrNullSafe()
                    if (!c.isNullOrBlank()) return c
                }
                is JsonObject -> {
                    val nested = e["name"] ?: e["username"] ?: e["title"] ?: e["slug"]
                    if (nested is JsonPrimitive && nested.content.isNotBlank()) return nested.content
                }
                else -> Unit
            }
        }
        return null
    }

    private fun JsonObject.longVal(vararg keys: String): Long? {
        for (k in keys) {
            (this[k] as? JsonPrimitive)?.content?.toLongOrNull()?.let { return it }
        }
        return null
    }

    private fun JsonObject.dblVal(vararg keys: String): Double? {
        for (k in keys) {
            (this[k] as? JsonPrimitive)?.content?.toDoubleOrNull()?.let { return it }
        }
        return null
    }

    private fun JsonObject.nestedSlug(key: String): String? {
        val obj = this[key] as? JsonObject ?: return null
        val slug = obj["slug"] as? JsonPrimitive ?: return null
        return slug.content.takeIf { it.isNotBlank() }
    }

    private fun JsonPrimitive.contentOrNullSafe(): String? {
        val c = content
        return if (c == "null") null else c
    }
}
