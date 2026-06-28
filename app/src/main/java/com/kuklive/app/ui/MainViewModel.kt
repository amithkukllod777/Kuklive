package com.kuklive.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kuklive.app.data.CategoryTaxonomy
import com.kuklive.app.data.PlaylistRepository
import com.kuklive.app.data.SettingsStore
import com.kuklive.app.data.model.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class Tab { CHANNELS, FAVORITES }

data class UiState(
    val settingsLoaded: Boolean = false,
    val setupNeeded: Boolean = false,
    val countryCodes: Set<String> = emptySet(),
    val languageCodes: Set<String> = emptySet(),
    val customUrl: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val channels: List<Channel> = emptyList(),
    val favoriteUrls: Set<String> = emptySet(),
    val query: String = "",
    val selectedCategory: String? = null,
    val tab: Tab = Tab.CHANNELS,
) {
    /** Curated genres present in the loaded channels, in industry-standard order. */
    val categories: List<String>
        get() = channels.flatMap { it.categories }
            .mapNotNull { CategoryTaxonomy.genreFor(it) }
            .distinct()
            .sortedWith(compareBy({ CategoryTaxonomy.orderIndex(it) }, { it }))

    val visibleChannels: List<Channel>
        get() {
            val base = if (tab == Tab.FAVORITES) {
                channels.filter { it.url in favoriteUrls }
            } else {
                channels
            }
            return base
                .filter { selectedCategory == null || it.categories.any { c -> CategoryTaxonomy.genreFor(c) == selectedCategory } }
                .filter { query.isBlank() || it.name.contains(query, ignoreCase = true) }
        }

    fun isFavorite(channel: Channel): Boolean = channel.url in favoriteUrls
}

class MainViewModel(
    private val repository: PlaylistRepository,
    private val settings: SettingsStore,
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    var playingChannels: List<Channel> = emptyList()
        private set
    var playingIndex: Int = 0
        private set

    private data class Persisted(
        val countries: Set<String>,
        val languages: Set<String>,
        val customUrl: String,
        val setupDone: Boolean,
        val favorites: Set<String>,
    )

    init {
        viewModelScope.launch {
            combine(
                settings.countryCodes,
                settings.languageCodes,
                settings.customUrl,
                settings.setupDone,
                settings.favoriteUrls,
            ) { countries, languages, customUrl, setupDone, favs ->
                Persisted(countries, languages, customUrl, setupDone, favs)
            }.collect { p ->
                val firstEmission = !_state.value.settingsLoaded
                _state.update {
                    it.copy(
                        settingsLoaded = true,
                        setupNeeded = !p.setupDone,
                        countryCodes = p.countries,
                        languageCodes = p.languages,
                        customUrl = p.customUrl,
                        favoriteUrls = p.favorites,
                    )
                }
                if (firstEmission && p.setupDone) reload()
            }
        }
    }

    /** Saves the country/language/custom choice and loads the matching channels. */
    fun applySetup(countries: Set<String>, languages: Set<String>, customUrl: String = "") {
        viewModelScope.launch {
            settings.saveSetup(countries, languages, customUrl)
            _state.update { it.copy(setupNeeded = false, selectedCategory = null) }
            reload(countries, languages, customUrl)
        }
    }

    fun reload(
        countries: Set<String> = _state.value.countryCodes,
        languages: Set<String> = _state.value.languageCodes,
        customUrl: String = _state.value.customUrl,
    ) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.loadChannels(countries, languages, customUrl)
                .onSuccess { list ->
                    _state.update { it.copy(isLoading = false, channels = list, error = null) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message ?: "Failed to load") }
                }
        }
    }

    fun onQueryChange(q: String) = _state.update { it.copy(query = q) }

    fun onCategorySelected(category: String?) = _state.update { it.copy(selectedCategory = category) }

    fun onTabSelected(tab: Tab) = _state.update { it.copy(tab = tab, selectedCategory = null) }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch { settings.toggleFavorite(channel.url) }
    }

    fun selectForPlayback(channel: Channel) {
        val list = _state.value.visibleChannels
        playingChannels = list
        playingIndex = list.indexOfFirst { it.id == channel.id }.coerceAtLeast(0)
    }

    fun updatePlayingIndex(index: Int) {
        if (index in playingChannels.indices) playingIndex = index
    }

    class Factory(
        private val repository: PlaylistRepository,
        private val settings: SettingsStore,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return MainViewModel(repository, settings) as T
        }
    }
}
