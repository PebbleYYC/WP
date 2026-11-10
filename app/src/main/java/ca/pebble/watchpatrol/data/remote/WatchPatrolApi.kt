package ca.pebble.watchpatrol.data.remote

import ca.pebble.watchpatrol.data.remote.dto.BrandDto
import ca.pebble.watchpatrol.data.remote.dto.BulkHistoryRequest
import ca.pebble.watchpatrol.data.remote.dto.IdentifyResponseDto
import ca.pebble.watchpatrol.data.remote.dto.ListingDto
import ca.pebble.watchpatrol.data.remote.dto.ModelDto
import ca.pebble.watchpatrol.data.remote.dto.Paginated
import ca.pebble.watchpatrol.data.remote.dto.PriceHistoryDto
import ca.pebble.watchpatrol.data.remote.dto.PricingDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/**
 * Endpoint paths are relative to [ApiConfig.BASE_URL]. Adjust paths / query
 * names here if the OpenAPI schema differs; the rest of the app is agnostic.
 */
interface WatchPatrolApi {

    // ── Listings feed (Reddit + 20+ forums, deduped) ────────────────────────
    @GET("listings/")
    suspend fun getListings(
        @Query("q") q: String? = null,
        @Query("brand") brand: String? = null,
        @Query("model") model: String? = null,
        @Query("source") source: String? = null,
        @Query("min_price") minPrice: Int? = null,
        @Query("max_price") maxPrice: Int? = null,
        @Query("ordering") ordering: String? = null,
        @Query("cursor") cursor: String? = null,
    ): Paginated<ListingDto>

    /** Follows an absolute `next` URL from a paginated response. */
    @GET
    suspend fun getListingsByUrl(@Url url: String): Paginated<ListingDto>

    @GET("listings/{id}/")
    suspend fun getListing(@Path("id") id: Long): ListingDto

    // ── Catalog: brands ─────────────────────────────────────────────────────
    @GET("brands/")
    suspend fun getBrands(
        @Query("q") q: String? = null,
        @Query("cursor") cursor: String? = null,
    ): Paginated<BrandDto>

    @GET
    suspend fun getBrandsByUrl(@Url url: String): Paginated<BrandDto>

    @GET("brands/{slug}/")
    suspend fun getBrand(@Path("slug") slug: String): BrandDto

    // ── Catalog: models ─────────────────────────────────────────────────────
    @GET("models/")
    suspend fun getModels(
        @Query("brand") brand: String? = null,
        @Query("q") q: String? = null,
        @Query("cursor") cursor: String? = null,
    ): Paginated<ModelDto>

    @GET
    suspend fun getModelsByUrl(@Url url: String): Paginated<ModelDto>

    @GET("models/{slug}/")
    suspend fun getModel(@Path("slug") slug: String): ModelDto

    // ── Pricing ─────────────────────────────────────────────────────────────
    @GET("pricing/{slug}/")
    suspend fun getPricing(
        @Path("slug") slug: String,
        @Query("currency") currency: String? = null,
    ): PricingDto

    @POST("pricing/bulk-history/")
    suspend fun getBulkHistory(@Body body: BulkHistoryRequest): List<PriceHistoryDto>

    // ── Visual identification ───────────────────────────────────────────────
    @Multipart
    @POST("identify/")
    suspend fun identify(@Part photo: MultipartBody.Part): IdentifyResponseDto
}
