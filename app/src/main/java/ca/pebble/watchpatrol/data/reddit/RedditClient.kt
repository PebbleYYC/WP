package ca.pebble.watchpatrol.data.reddit

import ca.pebble.watchpatrol.domain.model.Listing
import ca.pebble.watchpatrol.util.Formatters
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import okhttp3.Credentials
import retrofit2.HttpException

/**
 * Pulls watch-for-sale posts directly from r/Watchexchange via Reddit's
 * official API, using app-only (client_credentials) OAuth with the user's own
 * registered app credentials. Reads public listings only.
 */
class RedditClient(
    private val api: RedditApi,
    private val credsProvider: () -> RedditCreds?,
) {

    class NotConfigured : Exception(
        "Add your Reddit API key to use the feed (tap the key icon).",
    )

    private val mutex = Mutex()
    private val deviceId = java.util.UUID.randomUUID().toString()
    @Volatile private var token: String? = null
    @Volatile private var expiresAtMs: Long = 0

    private suspend fun bearer(): String {
        val creds = credsProvider() ?: throw NotConfigured()
        token?.let { if (System.currentTimeMillis() < expiresAtMs) return "bearer $it" }
        return mutex.withLock {
            token?.let { if (System.currentTimeMillis() < expiresAtMs) return@withLock "bearer $it" }
            val resp = fetchToken(creds)
            val access = resp.accessToken
                ?: throw IllegalStateException(
                    "Reddit returned no token (scope=${resp.scope ?: "?"}). Check the app type.",
                )
            token = access
            expiresAtMs = System.currentTimeMillis() + (resp.expiresIn - 60).coerceAtLeast(60) * 1000
            "bearer $access"
        }
    }

    /** Lets a fresh key take effect after the user updates it. */
    fun clearToken() {
        token = null
        expiresAtMs = 0
    }

    private suspend fun fetchToken(creds: RedditCreds): RedditToken {
        val id = creds.id
        val secret = creds.secret
        // 1) Confidential app (web app / script): client_credentials, basic id:secret.
        return try {
            api.token(
                url = RedditConfig.TOKEN_URL,
                basic = Credentials.basic(id, secret),
                userAgent = RedditConfig.USER_AGENT,
                fields = mapOf("grant_type" to "client_credentials"),
            )
        } catch (e: HttpException) {
            if (e.code() != 401 && e.code() != 403) throw redditError(e)
            // 2) Installed (public) app: userless grant, basic id with empty secret.
            try {
                api.token(
                    url = RedditConfig.TOKEN_URL,
                    basic = Credentials.basic(id, ""),
                    userAgent = RedditConfig.USER_AGENT,
                    fields = mapOf(
                        "grant_type" to "https://oauth.reddit.com/grants/installed_client",
                        "device_id" to deviceId,
                    ),
                )
            } catch (e2: HttpException) {
                throw redditError(e2)
            }
        }
    }

    private fun redditError(e: HttpException): Exception {
        val body = runCatching { e.response()?.errorBody()?.string() }.getOrNull()?.take(180)
        return IllegalStateException("Reddit auth HTTP ${e.code()}: ${body ?: e.message()}")
    }

    /** Returns (listings, nextAfterCursor). Reads Reddit's public .json feed — no key. */
    suspend fun fetch(query: String?, after: String?): Pair<List<Listing>, String?> {
        val sub = RedditConfig.SUBREDDIT
        val base = if (query.isNullOrBlank()) {
            "https://www.reddit.com/r/$sub/new.json"
        } else {
            val q = java.net.URLEncoder.encode(query, "UTF-8")
            "https://www.reddit.com/r/$sub/search.json?q=$q&restrict_sr=1&sort=new"
        }
        val sep = if (base.contains("?")) "&" else "?"
        val url = buildString {
            append(base).append(sep).append("limit=100&raw_json=1")
            if (!after.isNullOrBlank()) append("&after=").append(after)
        }
        val listing = api.publicListing(url, RedditConfig.BROWSER_UA)
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
