package ca.pebble.watchpatrol.data.reddit

import ca.pebble.watchpatrol.BuildConfig

/** Reddit app credentials + endpoints. Credentials come from BuildConfig (CI secret), not source. */
object RedditConfig {
    val CLIENT_ID: String = BuildConfig.REDDIT_CLIENT_ID.trim()
    val CLIENT_SECRET: String = BuildConfig.REDDIT_CLIENT_SECRET.trim()

    // Reddit requires a unique, descriptive User-Agent.
    const val USER_AGENT = "android:ca.pebble.watchpatrol:1.0.0 (personal watch aggregator)"
    const val SUBREDDIT = "Watchexchange"
    const val TOKEN_URL = "https://www.reddit.com/api/v1/access_token"

    val isConfigured: Boolean get() = CLIENT_ID.isNotBlank() && CLIENT_SECRET.isNotBlank()
}
