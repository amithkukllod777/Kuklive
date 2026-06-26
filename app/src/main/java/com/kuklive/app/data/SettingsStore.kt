package com.kuklive.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kuklive_settings")

/**
 * Persists the user's selected countries / languages (each a set — empty means
 * "all countries" / "any language") and their favorite channel URLs.
 */
class SettingsStore(private val context: Context) {

    private val countriesKey = stringSetPreferencesKey("country_codes")
    private val languagesKey = stringSetPreferencesKey("language_codes")
    private val setupDoneKey = booleanPreferencesKey("setup_done")
    private val favoritesKey = stringSetPreferencesKey("favorite_urls")

    val countryCodes: Flow<Set<String>> = context.dataStore.data.map { it[countriesKey] ?: emptySet() }
    val languageCodes: Flow<Set<String>> = context.dataStore.data.map { it[languagesKey] ?: emptySet() }
    val setupDone: Flow<Boolean> = context.dataStore.data.map { it[setupDoneKey] ?: false }
    val favoriteUrls: Flow<Set<String>> = context.dataStore.data.map { it[favoritesKey] ?: emptySet() }

    suspend fun saveSetup(countries: Set<String>, languages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[countriesKey] = countries.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
            prefs[languagesKey] = languages.map { it.trim() }.filter { it.isNotEmpty() }.toSet()
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
