package ca.pebble.watchpatrol.data.remote

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * Reads WatchRecon's public, server-rendered pages. WatchRecon aggregates
 * for-sale listings across Reddit + the major watch forums (WatchUSeek,
 * OmegaForums, RolexForums, TimeZone, …) — keyless, no login.
 *
 * Base URL: https://watchrecon.com/
 */
interface WatchReconWeb {

    /** Home feed (query = null) or search results, as raw HTML. */
    @Headers("Accept: text/html")
    @GET(".")
    suspend fun page(
        @Query("query") query: String? = null,
    ): String
}
