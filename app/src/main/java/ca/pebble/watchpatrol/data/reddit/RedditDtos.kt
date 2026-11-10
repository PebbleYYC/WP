package ca.pebble.watchpatrol.data.reddit

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RedditToken(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("token_type") val tokenType: String? = null,
    @SerialName("expires_in") val expiresIn: Long = 3600,
    val scope: String? = null,
)

@Serializable
data class RedditListing(
    val data: RedditListingData = RedditListingData(),
)

@Serializable
data class RedditListingData(
    val children: List<RedditChild> = emptyList(),
    val after: String? = null,
)

@Serializable
data class RedditChild(
    val data: RedditPost = RedditPost(),
)

@Serializable
data class RedditPost(
    val id: String = "",
    val title: String = "",
    val author: String? = null,
    @SerialName("created_utc") val createdUtc: Double = 0.0,
    val permalink: String? = null,
    val url: String? = null,
    val thumbnail: String? = null,
    val selftext: String? = null,
    @SerialName("link_flair_text") val flair: String? = null,
    val preview: RedditPreview? = null,
    val stickied: Boolean = false,
    @SerialName("over_18") val over18: Boolean = false,
)

@Serializable
data class RedditPreview(
    val images: List<RedditImage> = emptyList(),
)

@Serializable
data class RedditImage(
    val source: RedditImageSource? = null,
)

@Serializable
data class RedditImageSource(
    val url: String? = null,
)
