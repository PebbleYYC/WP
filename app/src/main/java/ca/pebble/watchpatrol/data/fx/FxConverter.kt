package ca.pebble.watchpatrol.data.fx

import ca.pebble.watchpatrol.domain.model.Listing
import ca.pebble.watchpatrol.util.Formatters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Converts listing prices to CAD using live rates (cached ~6h). Bare "$" is
 * treated as USD. If rates can't be fetched or the currency is unknown, the
 * original price is left untouched rather than showing a wrong number.
 */
class FxConverter(private val api: FrankfurterApi) {

    private val mutex = Mutex()
    @Volatile private var rates: Map<String, Double>? = null // X per 1 CAD
    @Volatile private var fetchedAt = 0L
    private val ttlMs = 6 * 60 * 60 * 1000L

    private suspend fun rates(): Map<String, Double> {
        rates?.let { if (System.currentTimeMillis() - fetchedAt < ttlMs) return it }
        return mutex.withLock {
            rates?.let { if (System.currentTimeMillis() - fetchedAt < ttlMs) return@withLock it }
            val fetched = runCatching { api.latestFromCad().rates + ("CAD" to 1.0) }.getOrNull()
            if (fetched != null && fetched.size > 1) {
                rates = fetched
                fetchedAt = System.currentTimeMillis()
                fetched
            } else {
                rates ?: emptyMap()
            }
        }
    }

    suspend fun toCadAll(listings: List<Listing>): List<Listing> {
        val r = rates()
        return listings.map { convert(it, r) }
    }

    private fun convert(listing: Listing, rates: Map<String, Double>): Listing {
        val amount = listing.price ?: return listing
        val currency = (listing.currency ?: "USD").uppercase()
        if (currency == "CAD") {
            return listing.copy(priceLabel = Formatters.cad(amount), originalPriceLabel = null)
        }
        val perCad = rates[currency] ?: return listing // unknown currency -> leave as-is
        if (perCad <= 0.0) return listing
        val cad = amount / perCad
        val original = listing.priceLabel.takeIf { it != "—" }?.let { "$it $currency" }
        return listing.copy(
            priceLabel = Formatters.cad(cad),
            originalPriceLabel = original,
        )
    }
}
