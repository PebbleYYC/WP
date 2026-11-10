@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package ca.pebble.watchpatrol.di

import android.content.Context
import ca.pebble.watchpatrol.BuildConfig
import ca.pebble.watchpatrol.data.local.StarredStore
import ca.pebble.watchpatrol.data.remote.ApiConfig
import ca.pebble.watchpatrol.data.remote.WatchPatrolApi
import ca.pebble.watchpatrol.data.remote.WatchPatrolWeb
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
import retrofit2.converter.scalars.ScalarsConverterFactory

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
                // Browser-like UA so the public pages serve us normally.
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14; Pixel) AppleWebKit/537.36 " +
                        "(KHTML, like Gecko) Chrome/125.0 Mobile Safari/537.36",
                )
            if (request.header("Accept") == null) {
                builder.header("Accept", "application/json")
            }
            ApiConfig.API_KEY?.let { key ->
                builder.header(ApiConfig.API_KEY_HEADER, ApiConfig.apiKeyValue(key))
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

    // JSON partner API (catalog / pricing / identify) — needs a key.
    private val apiRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // Public website (HTML) — keyless, used for the listings feed.
    private val webRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://www.watchpatrol.net/")
        .client(okHttp)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    val api: WatchPatrolApi = apiRetrofit.create(WatchPatrolApi::class.java)
    val web: WatchPatrolWeb = webRetrofit.create(WatchPatrolWeb::class.java)

    val listingsRepository = ListingsRepository(web)
    val catalogRepository = CatalogRepository(api)
    val pricingRepository = PricingRepository(api)
    val identifyRepository = IdentifyRepository(api)
    val starredStore = StarredStore(context)
}
