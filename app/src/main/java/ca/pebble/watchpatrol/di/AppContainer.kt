@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package ca.pebble.watchpatrol.di

import android.content.Context
import ca.pebble.watchpatrol.BuildConfig
import ca.pebble.watchpatrol.data.local.StarredStore
import ca.pebble.watchpatrol.data.reddit.RedditApi
import ca.pebble.watchpatrol.data.reddit.RedditClient
import ca.pebble.watchpatrol.data.remote.ApiConfig
import ca.pebble.watchpatrol.data.remote.WatchPatrolApi
import ca.pebble.watchpatrol.data.repository.CatalogRepository
import ca.pebble.watchpatrol.data.repository.IdentifyRepository
import ca.pebble.watchpatrol.data.repository.ListingsRepository
import ca.pebble.watchpatrol.data.repository.PricingRepository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/** Hand-rolled dependency graph — no Hilt / kapt, so it builds clean anywhere. */
class AppContainer(context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    private val okHttp: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()
            val builder = request.newBuilder()
            if (request.header("User-Agent") == null) {
                builder.header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Pixel) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/125.0 Mobile Safari/537.36",
                )
            }
            if (request.header("Accept") == null) {
                builder.header("Accept", "application/json")
            }
            if (request.header("Authorization") == null) {
                ApiConfig.API_KEY?.let { key ->
                    builder.header(ApiConfig.API_KEY_HEADER, ApiConfig.apiKeyValue(key))
                }
            }
            chain.proceed(builder.build())
        }
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC },
                )
            }
        }
        .build()

    private val jsonFactory = json.asConverterFactory("application/json".toMediaType())

    // WatchPatrol partner API (catalog / pricing / identify) — needs a key.
    private val apiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttp)
        .addConverterFactory(jsonFactory)
        .build()

    // Reddit official API — listings source for Discover & Search.
    private val redditRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://oauth.reddit.com/")
        .client(okHttp)
        .addConverterFactory(jsonFactory)
        .build()

    val api: WatchPatrolApi = apiRetrofit.create(WatchPatrolApi::class.java)
    private val redditApi: RedditApi = redditRetrofit.create(RedditApi::class.java)
    private val redditClient = RedditClient(redditApi)

    val listingsRepository = ListingsRepository(redditClient)
    val catalogRepository = CatalogRepository(api)
    val pricingRepository = PricingRepository(api)
    val identifyRepository = IdentifyRepository(api)
    val starredStore = StarredStore(context)
}
