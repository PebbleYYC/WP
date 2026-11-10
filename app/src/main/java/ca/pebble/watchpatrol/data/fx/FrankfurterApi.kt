package ca.pebble.watchpatrol.data.fx

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/** Free, keyless ECB FX rates. Base URL: https://api.frankfurter.app/ */
interface FrankfurterApi {

    /** rates[X] = how many X per 1 CAD (so amountCAD = amountX / rates[X]). */
    @GET("latest")
    suspend fun latestFromCad(@Query("base") base: String = "CAD"): FxResponse
}

@Serializable
data class FxResponse(
    val base: String = "CAD",
    val date: String? = null,
    val rates: Map<String, Double> = emptyMap(),
)
