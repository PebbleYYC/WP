@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package ca.pebble.watchpatrol.di

import android.content.Context
import ca.pebble.watchpatrol.BuildConfig
import ca.pebble.watchpatrol.data.local.StarredStore
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
            val builder = chain.request().newBuilder()
                .header("Accept", "application/json")
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

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .client(okHttp)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    val api: WatchPatrolApi = retrofit.create(WatchPatrolApi::class.java)

    val listingsRepository = ListingsRepository(api)
    val catalogRepository = CatalogRepository(api)
    val pricingRepository = PricingRepository(api)
    val identifyRepository = IdentifyRepository(api)
    val starredStore = StarredStore(context)
}
