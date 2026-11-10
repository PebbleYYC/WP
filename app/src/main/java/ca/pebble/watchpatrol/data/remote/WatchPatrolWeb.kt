package ca.pebble.watchpatrol.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Reads WatchPatrol's PUBLIC pages (the same server-rendered HTML any browser
 * loads — no login, no key) so the feed works without the gated partner API.
 * The listings endpoint is key-walled; this is the keyless public surface.
 *
 * Base URL: https://www.watchpatrol.net/
 */
interface WatchPatrolWeb {

    /** Home feed (query = null) or search results, as raw HTML. */
    @Headers("Accept: text/html")
    @GET(".")
    suspend fun page(
        @Query("query") query: String? = null,
        @Query("page") page: Int? = null,
    ): String
}
