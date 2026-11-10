package ca.pebble.watchpatrol.data.scrape

import ca.pebble.watchpatrol.domain.model.Listing
import org.jsoup.Jsoup

/**
 * Parses WatchRecon's public listing grid into domain [Listing]s.
 *
 * Card shape (as of 2026-11):
 *   div.galleryItemContainer
 *     a.listingLink[href=<forum post url>, title=<title>]  -> title + original link
 *     img.thumb[src=image.php?image_id=…]                  -> image
 *     a[href=detail.php?cid=<id>] .priceInfo               -> id + price
 *     .userNameInfo .sourceInfo .postDateInfo .brandInfo .modelInfo
 *
 * These selectors are the single point of repair if WatchRecon restyles.
 */
object WatchReconScraper {

    private const val SITE = "https://watchrecon.com/"
    private val CID = Regex("cid=(\\d+)")

    fun parse(html: String): List<Listing> {
        val doc = Jsoup.parse(html, SITE)
        return doc.select("div.galleryItemContainer").mapNotNull { el ->
            // The title link lives in .subjectInfo; the thumbnail is also wrapped
            // in a link, so target the subject one specifically.
            val link = el.selectFirst(".subjectInfo a") ?: el.selectFirst("a.listingLink")
            val original = link?.absUrl("href")?.takeIf { it.isNotBlank() }
                ?: link?.attr("href")?.takeIf { it.isNotBlank() }

            val title = link?.attr("title")?.takeIf { it.isNotBlank() }
                ?: link?.text()?.takeIf { it.isNotBlank() }
                ?: "Untitled listing"

            val detailHref = el.selectFirst("a[href*=detail.php]")?.attr("href")
            val id = detailHref?.let { CID.find(it)?.groupValues?.get(1)?.toLongOrNull() }
                ?: original?.let { stableId(it) }
                ?: return@mapNotNull null

            val priceText = el.selectFirst(".priceInfo")?.text()?.takeIf { it.isNotBlank() }
            val priceValue = priceText?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull()
            val currency = when {
                priceText == null -> null
                priceText.contains("€") -> "EUR"
                priceText.contains("£") -> "GBP"
                priceText.contains("$") -> "USD"
                else -> null
            }

            val image = el.selectFirst("img.thumb")?.absUrl("src")?.takeIf { it.isNotBlank() }
            val seller = el.selectFirst(".userNameInfo")?.text()?.takeIf { it.isNotBlank() }
            val source = el.selectFirst(".sourceInfo")?.text()?.takeIf { it.isNotBlank() }
            val posted = el.selectFirst(".postDateInfo")?.text()?.takeIf { it.isNotBlank() }
            val brand = el.selectFirst(".brandInfo")?.text()?.takeIf { it.isNotBlank() }
            val model = el.selectFirst(".modelInfo")?.text()?.takeIf { it.isNotBlank() }

            Listing(
                id = id,
                title = title,
                price = priceValue,
                currency = currency,
                priceLabel = priceText ?: "—",
                source = source,
                seller = seller,
                imageUrl = image,
                originalUrl = original,
                postedRelative = posted,
                brandName = brand,
                modelName = model,
                modelSlug = null,
            )
        }
    }

    private fun stableId(s: String): Long = s.hashCode().toLong().let { if (it < 0) -it else it }
}
