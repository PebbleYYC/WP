package ca.pebble.watchpatrol.data.remote

/**
 * Everything environment-specific about the API lives here. If the live
 * OpenAPI schema uses a different base path or requires a key, this is the
 * only file you touch for connectivity.
 */
object ApiConfig {
    // TODO: confirm against watchpatrol.net OpenAPI schema. Must end with "/".
    const val BASE_URL = "https://www.watchpatrol.net/api/"

    // If the listings/catalog endpoints require a key, put it here (or better,
    // inject from BuildConfig / a secrets file). Leave null for public access.
    val API_KEY: String? = null

    // Header + value scheme used when API_KEY is set, e.g.:
    //   "Authorization" -> "Bearer <key>"   or   "Authorization" -> "Api-Key <key>"
    const val API_KEY_HEADER = "Authorization"
    fun apiKeyValue(key: String) = "Bearer $key"
}
