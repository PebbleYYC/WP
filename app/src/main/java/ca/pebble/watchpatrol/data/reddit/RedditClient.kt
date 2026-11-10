package ca.pebble.watchpatrol.data.reddit

import ca.pebble.watchpatrol.domain.model.Listing
import ca.pebble.watchpatrol.util.Formatters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Credentials

/**
 * Pulls watch-for-sale posts directly from r/Watchexchange via Reddit's
 * official API, using app-only (client_credentials) OAuth with the user's own
 * registered app credentials. Reads public listings only.
 */
class RedditClient(private val api: RedditApi) {

    class NotConfigured : Exception(
        "Add your Reddit API key to use the feed (client id + secret).",
    )

    private val mutex = Mutex()
    @Volatile private var token: String? = null
    @Volatile private var expiresAtMs: Long = 0

    private suspend fun bearer(): String {
        if (!RedditConfig.isConfigured) throw NotConfigured()
        token?.let { if (System.currentTimeMillis() < expiresAtMs) return "bearer $it" }
        return mutex.withLock {
            token?.let { if (System.currentTimeMillis() < expiresAtMs) return@withLock "bearer $it" }
            val resp = api.token(
                url = RedditConfig.TOKEN_URL,
                basic = Credentials.basic(RedditConfig.CLIENT_ID, RedditConfig.CLIENT_SECRET),
                userAgent = RedditConfig.USER_AGENT,
            )
            val access = resp.accessToken
                ?: throw IllegalStateException("Reddit auth failed — check the client id/secret")
            token = access
            expiresAtMs = System.currentTimeMillis() + (resp.expiresIn - 60).coerceAtLeast(60) * 1000
            "bearer $access"
        }
    }

    /** Returns (listings, nextAfterCursor). */
    suspend fun fetch(query: String?, after: String?): Pair<List<Listing>, String?> {
        val auth = bearer()
        val listing = if (query.isNullOrBlank()) {
            api.new(RedditConfig.SUBREDDIT, auth, RedditConfig.USER_AGENT, after = after)
        } else {
            api.search(RedditConfig.SUBREDDIT, auth, RedditConfig.USER_AGENT, q = query, after = after)
        }
        val items = listing.data.children.mapNotNull { it.data.toListing() }
        return items to listing.data.after
    }

    private fun RedditPost.toListing(): Listing? {
        if (stickied || over18 || id.isBlank()) return null
        val upper = title.uppercase()
        if (upper.startsWith("[WTB]") || upper.startsWith("[META]") ||
            flair.equals("Meta", ignoreCase = true) || flair.equals("Mod Post", ignoreCase = true)
        ) return null

        val listingId = id.toLongOrNull(36) ?: id.hashCode().toLong().let { if (it < 0) -it else it }
        val cleanTitle = title
            .replace(Regex("^(\\s*\\[[^\\]]*\\]\\s*)+"), "")
            .trim()
            .ifBlank { title }

        val priceRegex = Regex("[$€£]\\s?\\d[\\d,]*(?:\\.\\d{1,2})?")
        val priceMatch = priceRegex.find(title)?.value ?: priceRegex.find(selftext.orEmpty())?.value
        val priceLabel = priceMatch?.replace(" ", "")
        val priceValue = priceLabel?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull()
        val currency = when {
            priceLabel == null -> null
            priceLabel.contains("€") -> "EUR"
            priceLabel.contains("£") -> "GBP"
            priceLabel.contains("$") -> "USD"
            else -> null
        }

        val previewImage = preview?.images?.firstOrNull()?.source?.url
            ?.replace("&amp;", "&")?.takeIf { it.isNotBlank() }
        val directImage = url?.takeIf { u ->
            u.contains("i.redd.it") || u.contains("imgur") ||
                Regex(".*\\.(jpg|jpeg|png|webp)(\\?.*)?$", RegexOption.IGNORE_CASE).matches(u)
        }
        val thumb = thumbnail?.takeIf { it.startsWith("http") }

        return Listing(
            id = listingId,
            title = cleanTitle,
            price = priceValue,
            currency = currency,
            priceLabel = priceLabel ?: "—",
            source = "r/${RedditConfig.SUBREDDIT}",
            seller = author?.let { "u/$it" },
            imageUrl = previewImage ?: directImage ?: thumb,
            originalUrl = permalink?.let { "https://www.reddit.com$it" } ?: url,
            postedRelative = Formatters.relativeTimeEpoch(createdUtc.toLong()),
            brandName = null,
            modelName = null,
            modelSlug = null,
        )
    }
}
