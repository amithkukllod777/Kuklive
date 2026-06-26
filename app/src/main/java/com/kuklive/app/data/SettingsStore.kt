package com.kuklive.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kuklive_settings")

/**
 * Persists the user's country / language selection (used to load just the
 * channels they want) and their favorite channel URLs.
 */
class SettingsStore(private val context: Context) {

    private val countryKey = stringPreferencesKey("country_code")
    private val languageKey = stringPreferencesKey("language_code")
    private val setupDoneKey = booleanPreferencesKey("setup_done")
    private val favoritesKey = stringSetPreferencesKey("favorite_urls")

    val countryCode: Flow<String> = context.dataStore.data.map { it[countryKey] ?: DEFAULT_COUNTRY }
    val languageCode: Flow<String> = context.dataStore.data.map { it[languageKey] ?: "" }
    val setupDone: Flow<Boolean> = context.dataStore.data.map { it[setupDoneKey] ?: false }
    val favoriteUrls: Flow<Set<String>> = context.dataStore.data.map { it[favoritesKey] ?: emptySet() }

    suspend fun saveSetup(country: String, language: String) {
        context.dataStore.edit { prefs ->
            prefs[countryKey] = country.trim()
            prefs[languageKey] = language.trim()
            prefs[setupDoneKey] = true
        }
    }

    suspend fun toggleFavorite(url: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[favoritesKey] ?: emptySet()
            prefs[favoritesKey] = if (url in current) current - url else current + url
        }
    }

    companion object {
        const val DEFAULT_COUNTRY = "in"
    }
}
