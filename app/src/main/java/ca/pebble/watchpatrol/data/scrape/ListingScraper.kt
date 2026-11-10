package ca.pebble.watchpatrol.data.scrape

import ca.pebble.watchpatrol.domain.model.Listing
import ca.pebble.watchpatrol.util.Formatters
import org.jsoup.Jsoup

/**
 * Parses WatchPatrol's public listing pages into domain [Listing]s.
 *
 * Card shape (stable as of 2026-11):
 *   div.panel#listing-<id>
 *     a[href=/listing/<id>/]  img.panel-thumbnail[src,alt]
 *     .post-title a            -> title
 *     .panel-username          -> seller
 *     time.panel-date[datetime]-> posted time
 *     img.source-icon[alt]     -> source (Reddit, forum, …)
 *     .price                   -> formatted price text
 *     ul.tags-new              -> "new" flag (unused for now)
 *
 * If WatchPatrol changes this markup the selectors here are the single point
 * of repair; nothing else in the app depends on the HTML.
 */
object ListingScraper {

    private const val SITE = "https://www.watchpatrol.net"

    fun parse(html: String): List<Listing> {
        val doc = Jsoup.parse(html, SITE)
        return doc.select("div.panel[id^=listing-]").mapNotNull { panel ->
            val id = panel.id().removePrefix("listing-").toLongOrNull()
                ?: panel.selectFirst("a[href^=/listing/]")
                    ?.attr("href")?.trim('/')?.removePrefix("listing/")?.toLongOrNull()
                ?: return@mapNotNull null

            val title = panel.selectFirst(".post-title")?.text()?.takeIf { it.isNotBlank() }
                ?: panel.selectFirst("img.panel-thumbnail")?.attr("alt")?.takeIf { it.isNotBlank() }
                ?: "Untitled listing"

            val image = panel.selectFirst("img.panel-thumbnail")?.absUrl("src")
                ?.takeIf { it.isNotBlank() }
                ?: panel.selectFirst("img.panel-thumbnail")?.attr("src")?.takeIf { it.isNotBlank() }

            val seller = panel.selectFirst(".panel-username")?.text()?.takeIf { it.isNotBlank() }

            val postedRelative = panel.selectFirst("time.panel-date")?.let { t ->
                Formatters.relativeTime(t.attr("datetime")) ?: t.text().takeIf { it.isNotBlank() }
            }

            val sourceIcon = panel.selectFirst("img.source-icon")
            val source = (sourceIcon?.attr("alt")?.takeIf { it.isNotBlank() }
                ?: sourceIcon?.attr("title"))?.takeIf { it.isNotBlank() }

            val priceText = panel.selectFirst(".price")?.text()?.takeIf { it.isNotBlank() }
            val priceValue = priceText?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull()
            val currency = when {
                priceText == null -> null
                priceText.contains("€") -> "EUR"
                priceText.contains("£") -> "GBP"
                priceText.contains("¥") -> "JPY"
                priceText.contains("$") -> "USD"
                else -> null
            }

            Listing(
                id = id,
                title = title,
                price = priceValue,
                currency = currency,
                priceLabel = priceText ?: Formatters.price(priceValue, currency),
                source = source,
                seller = seller,
                imageUrl = image,
                originalUrl = "$SITE/listing/$id/",
                postedRelative = postedRelative,
                brandName = null,
                modelName = null,
                modelSlug = null,
            )
        }
    }
}
