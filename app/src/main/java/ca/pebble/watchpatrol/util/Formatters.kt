package ca.pebble.watchpatrol.util

import android.text.format.DateUtils
import java.text.NumberFormat
import java.time.Instant
import java.util.Currency
import java.util.Locale
import kotlin.math.roundToLong

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

    /** Whole-dollar CAD, e.g. "CA$1,234". */
    fun cad(amount: Double): String =
        "CA$" + NumberFormat.getIntegerInstance(Locale.CANADA).format(amount.roundToLong())

    /** Best-guess ISO currency code from a price string's symbol/text. Bare "$" -> USD. */
    fun detectCurrency(priceText: String): String? = when {
        priceText.contains("€") -> "EUR"
        priceText.contains("£") -> "GBP"
        priceText.contains("¥") || priceText.contains("JPY", true) -> "JPY"
        priceText.contains("CHF", true) || priceText.contains("Fr", true) -> "CHF"
        Regex("CA\\\$|C\\\$|CAD", RegexOption.IGNORE_CASE).containsMatchIn(priceText) -> "CAD"
        Regex("A\\\$|AUD", RegexOption.IGNORE_CASE).containsMatchIn(priceText) -> "AUD"
        priceText.contains("$") -> "USD"
        else -> null
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

    /** Unix epoch seconds -> "3 hours ago". */
    fun relativeTimeEpoch(epochSeconds: Long): String? {
        if (epochSeconds <= 0) return null
        return DateUtils.getRelativeTimeSpanString(
            epochSeconds * 1000,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
        ).toString()
    }
}
