@file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package ca.pebble.watchpatrol.di

import android.content.Context
import ca.pebble.watchpatrol.BuildConfig
import ca.pebble.watchpatrol.data.fx.FrankfurterApi
import ca.pebble.watchpatrol.data.fx.FxConverter
import ca.pebble.watchpatrol.data.local.SettingsStore
import ca.pebble.watchpatrol.data.local.StarredStore
import ca.pebble.watchpatrol.data.reddit.RedditApi
import ca.pebble.watchpatrol.data.reddit.RedditClient
import ca.pebble.watchpatrol.data.reddit.RedditConfig
import ca.pebble.watchpatrol.data.reddit.RedditCreds
import ca.pebble.watchpatrol.data.remote.ApiConfig
import ca.pebble.watchpatrol.data.remote.WatchPatrolApi
import ca.pebble.watchpatrol.data.remote.WatchReconWeb
import ca.pebble.watchpatrol.data.repository.CatalogRepository
import ca.pebble.watchpatrol.data.repository.IdentifyRepository
import ca.pebble.watchpatrol.data.repository.ListingsRepository
import ca.pebble.watchpatrol.data.repository.PricingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
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
                    HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY },
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

    // WatchRecon public pages (HTML) — keyless listings source.
    private val watchReconRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://watchrecon.com/")
        .client(okHttp)
        .addConverterFactory(ScalarsConverterFactory.create())
        .build()

    // Live FX for converting listing prices to CAD (keyless).
    private val frankfurterRetrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.frankfurter.app/")
        .client(okHttp)
        .addConverterFactory(jsonFactory)
        .build()

    val api: WatchPatrolApi = apiRetrofit.create(WatchPatrolApi::class.java)
    private val redditApi: RedditApi = redditRetrofit.create(RedditApi::class.java)
    private val watchReconWeb: WatchReconWeb = watchReconRetrofit.create(WatchReconWeb::class.java)
    private val fxConverter = FxConverter(frankfurterRetrofit.create(FrankfurterApi::class.java))

    val settingsStore = SettingsStore(context)
    val starredStore = StarredStore(context)

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // In-app key wins; otherwise fall back to any build-time credentials.
    @Volatile private var redditCreds: RedditCreds? = RedditConfig.buildConfigCreds()

    private val redditClient = RedditClient(redditApi) { redditCreds }

    init {
        appScope.launch {
            settingsStore.redditCreds.collect { entered ->
                redditCreds = entered ?: RedditConfig.buildConfigCreds()
                redditClient.clearToken()
            }
        }
    }

    val listingsRepository = ListingsRepository(watchReconWeb, fxConverter)
    val catalogRepository = CatalogRepository(api)
    val pricingRepository = PricingRepository(api)
    val identifyRepository = IdentifyRepository(api)
}
