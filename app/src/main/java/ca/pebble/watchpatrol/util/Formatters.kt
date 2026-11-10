package ca.pebble.watchpatrol.util

import android.text.format.DateUtils
import java.text.NumberFormat
import java.time.Instant
import java.util.Currency
import java.util.Locale

object Formatters {

    /** Formats a price with its ISO currency, falling back gracefully. */
    fun price(amount: Double?, currency: String?): String {
        if (amount == null) return "—"
        val fmt = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            maximumFractionDigits = if (amount >= 1000) 0 else 2
            try {
                if (!currency.isNullOrBlank()) this.currency = Currency.getInstance(currency.uppercase())
            } catch (_: IllegalArgumentException) {
                // Unknown currency code — keep the default symbol.
            }
        }
        return runCatching { fmt.format(amount) }.getOrElse {
            "${currency?.uppercase().orEmpty()} ${amount.toLong()}".trim()
        }
    }

    fun priceCompact(amount: Double?, currency: String?): String {
        if (amount == null) return "—"
        val symbol = runCatching {
            if (!currency.isNullOrBlank()) Currency.getInstance(currency.uppercase()).symbol else "$"
        }.getOrDefault("$")
        return when {
            amount >= 1_000_000 -> "$symbol${"%.1f".format(amount / 1_000_000)}M"
            amount >= 1_000 -> "$symbol${"%.1f".format(amount / 1_000)}k"
            else -> "$symbol${amount.toLong()}"
        }
    }

    /** ISO-8601 timestamp -> "3 days ago". Returns null on parse failure. */
    fun relativeTime(iso: String?): String? {
        if (iso.isNullOrBlank()) return null
        val millis = runCatching { Instant.parse(iso).toEpochMilli() }.getOrNull() ?: return null
        return DateUtils.getRelativeTimeSpanString(
            millis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        ).toString()
    }
}
