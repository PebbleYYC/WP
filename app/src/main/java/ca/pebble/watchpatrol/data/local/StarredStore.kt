package ca.pebble.watchpatrol.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.pebble.watchpatrol.domain.model.Listing
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "watchpatrol")

/** Persists starred listings locally so they survive offline / restarts. */
class StarredStore(context: Context) {

    private val store = context.applicationContext.dataStore
    private val json = Json { ignoreUnknownKeys = true }
    private val key = stringPreferencesKey("starred_listings_json")

    val starred: Flow<List<Listing>> = store.data.map { prefs ->
        prefs[key]?.let { raw ->
            runCatching { json.decodeFromString<List<Listing>>(raw) }.getOrDefault(emptyList())
        } ?: emptyList()
    }

    val starredIds: Flow<Set<Long>> = starred.map { list -> list.map { it.id }.toSet() }

    suspend fun toggle(listing: Listing) {
        store.edit { prefs ->
            val current = prefs[key]?.let {
                runCatching { json.decodeFromString<List<Listing>>(it) }.getOrDefault(emptyList())
            } ?: emptyList()
            val updated = if (current.any { it.id == listing.id }) {
                current.filterNot { it.id == listing.id }
            } else {
                listOf(listing) + current
            }
            prefs[key] = json.encodeToString(updated)
        }
    }
}
