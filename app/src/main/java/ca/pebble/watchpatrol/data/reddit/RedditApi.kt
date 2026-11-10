package ca.pebble.watchpatrol.data.reddit

import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

/** Base URL: https://oauth.reddit.com/  (token endpoint uses an absolute @Url). */
interface RedditApi {

    @FormUrlEncoded
    @POST
    suspend fun token(
        @Url url: String,
        @Header("Authorization") basic: String,
        @Header("User-Agent") userAgent: String,
        @Field("grant_type") grantType: String = "client_credentials",
    ): RedditToken

    @GET("r/{sub}/new")
    suspend fun new(
        @Path("sub") sub: String,
        @Header("Authorization") bearer: String,
        @Header("User-Agent") userAgent: String,
        @Query("limit") limit: Int = 100,
        @Query("after") after: String? = null,
        @Query("raw_json") rawJson: Int = 1,
    ): RedditListing

    @GET("r/{sub}/search")
    suspend fun search(
        @Path("sub") sub: String,
        @Header("Authorization") bearer: String,
        @Header("User-Agent") userAgent: String,
        @Query("q") q: String,
        @Query("restrict_sr") restrictToSubreddit: Int = 1,
        @Query("sort") sort: String = "new",
        @Query("limit") limit: Int = 100,
        @Query("after") after: String? = null,
        @Query("raw_json") rawJson: Int = 1,
    ): RedditListing
}
