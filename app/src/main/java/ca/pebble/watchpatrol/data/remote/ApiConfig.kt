package ca.pebble.watchpatrol.data.remote

/**
 * Everything environment-specific about the API lives here. If the live
 * OpenAPI schema uses a different base path or requires a key, this is the
 * only file you touch for connectivity.
 */
object ApiConfig {
    // WatchPatrol public REST API. Confirmed base path (docs: /api/public/v1/docs/).
    const val BASE_URL = "https://www.watchpatrol.net/api/public/v1/"

    // The listings/pricing endpoints are gated behind a beta partner key
    // (request at watchpatrol.net/contact). Paste it here — sent as
    // "Authorization: Bearer <key>". Leave null to try unauthenticated.
    val API_KEY: String? = null

    // Header + value scheme used when API_KEY is set, e.g.:
    //   "Authorization" -> "Bearer <key>"   or   "Authorization" -> "Api-Key <key>"
    const val API_KEY_HEADER = "Authorization"
    fun apiKeyValue(key: String) = "Bearer $key"
}
