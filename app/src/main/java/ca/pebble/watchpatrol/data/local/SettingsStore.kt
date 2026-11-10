package ca.pebble.watchpatrol.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import ca.pebble.watchpatrol.data.reddit.RedditCreds
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "wp_settings")

/** Stores user-entered settings (currently the Reddit API key) on-device. */
class SettingsStore(context: Context) {

    private val store = context.applicationContext.settingsDataStore
    private val idKey = stringPreferencesKey("reddit_client_id")
    private val secretKey = stringPreferencesKey("reddit_client_secret")

    val redditCreds: Flow<RedditCreds?> = store.data.map { prefs ->
        val id = prefs[idKey]?.trim()
        val secret = prefs[secretKey]?.trim()
        if (!id.isNullOrBlank() && !secret.isNullOrBlank()) RedditCreds(id, secret) else null
    }

    suspend fun setRedditCreds(id: String, secret: String) {
        store.edit { prefs ->
            prefs[idKey] = id.trim()
            prefs[secretKey] = secret.trim()
        }
    }
}
