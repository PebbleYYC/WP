package ca.pebble.watchpatrol.data.reddit

import ca.pebble.watchpatrol.BuildConfig

/** Reddit app credentials entered in-app, or baked in via BuildConfig. */
data class RedditCreds(val id: String, val secret: String)

/** Reddit endpoints + the build-time credential fallback. */
object RedditConfig {
    private val BUILD_ID: String = BuildConfig.REDDIT_CLIENT_ID.trim()
    private val BUILD_SECRET: String = BuildConfig.REDDIT_CLIENT_SECRET.trim()

    // Reddit requires a unique, descriptive User-Agent.
    const val USER_AGENT = "android:ca.pebble.watchpatrol:1.0.0 (personal watch aggregator)"

    // Browser-like UA for the public .json feed (residential IPs are served).
    const val BROWSER_UA =
        "Mozilla/5.0 (Linux; Android 14; Pixel) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/125.0 Mobile Safari/537.36"

    const val SUBREDDIT = "Watchexchange"
    const val TOKEN_URL = "https://www.reddit.com/api/v1/access_token"

    /** Credentials baked in at build time, if any. In-app entry takes priority. */
    fun buildConfigCreds(): RedditCreds? =
        if (BUILD_ID.isNotBlank() && BUILD_SECRET.isNotBlank()) RedditCreds(BUILD_ID, BUILD_SECRET) else null
}
