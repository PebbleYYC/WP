package ca.pebble.watchpatrol.data.repository

import ca.pebble.watchpatrol.data.remote.WatchPatrolApi
import ca.pebble.watchpatrol.domain.model.IdentifyMatch
import ca.pebble.watchpatrol.domain.model.toDomain
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class IdentifyRepository(private val api: WatchPatrolApi) {

    /**
     * Uploads a photo and returns the nearest-matching models.
     * NOTE: the multipart field name ("image") is a best guess — adjust to
     * match the OpenAPI schema if identification returns 400.
     */
    suspend fun identify(bytes: ByteArray, filename: String = "photo.jpg"): List<IdentifyMatch> {
        val body = bytes.toRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("image", filename, body)
        return api.identify(part).matches.map { it.toDomain() }
    }
}
