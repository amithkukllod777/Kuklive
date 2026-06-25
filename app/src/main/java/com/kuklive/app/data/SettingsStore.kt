package com.kuklive.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kuklive_settings")

/**
 * Persists the active playlist URL and the set of favorite channel URLs.
 */
class SettingsStore(private val context: Context) {

    private val playlistKey = stringPreferencesKey("playlist_url")
    private val favoritesKey = stringSetPreferencesKey("favorite_urls")

    val playlistUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[playlistKey] ?: DEFAULT_PLAYLIST_URL
    }

    val favoriteUrls: Flow<Set<String>> = context.dataStore.data.map { prefs ->
        prefs[favoritesKey] ?: emptySet()
    }

    suspend fun setPlaylistUrl(url: String) {
        context.dataStore.edit { it[playlistKey] = url.trim() }
    }

    suspend fun toggleFavorite(url: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] = if (url in current) current - url else current + url
        }
    }

    companion object {
        /**
         * iptv-org's community-maintained index of publicly available streams.
         * Users can replace this with their own provider's M3U URL in Settings.
         */
        const val DEFAULT_PLAYLIST_URL = "https://iptv-org.github.io/iptv/index.m3u"
    }
}
