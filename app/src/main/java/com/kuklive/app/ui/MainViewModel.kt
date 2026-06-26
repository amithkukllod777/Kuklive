package com.kuklive.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
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
    val countryCode: String = SettingsStore.DEFAULT_COUNTRY,
    val languageCode: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val channels: List<Channel> = emptyList(),
    val favoriteUrls: Set<String> = emptySet(),
    val query: String = "",
    val selectedCategory: String? = null,
    val tab: Tab = Tab.CHANNELS,
) {
    val categories: List<String>
        get() = channels.flatMap { it.categories }.distinct().sorted()

    val visibleChannels: List<Channel>
        get() {
            val base = if (tab == Tab.FAVORITES) {
                channels.filter { it.url in favoriteUrls }
            } else {
                channels
            }
            return base
                .filter { selectedCategory == null || selectedCategory in it.categories }
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
        val country: String,
        val language: String,
        val setupDone: Boolean,
        val favorites: Set<String>,
    )

    init {
        viewModelScope.launch {
            combine(
                settings.countryCode,
                settings.languageCode,
                settings.setupDone,
                settings.favoriteUrls,
            ) { country, language, setupDone, favs ->
                Persisted(country, language, setupDone, favs)
            }.collect { p ->
                val firstEmission = !_state.value.settingsLoaded
                _state.update {
                    it.copy(
                        settingsLoaded = true,
                        setupNeeded = !p.setupDone,
                        countryCode = p.country,
                        languageCode = p.language,
                        favoriteUrls = p.favorites,
                    )
                }
                if (firstEmission && p.setupDone) reload()
            }
        }
    }

    /** Saves the country/language choice and loads the matching channels. */
    fun applySetup(country: String, language: String) {
        viewModelScope.launch {
            settings.saveSetup(country, language)
            _state.update { it.copy(setupNeeded = false, selectedCategory = null) }
            reload(country, language)
        }
    }

    fun reload(
        country: String = _state.value.countryCode,
        language: String = _state.value.languageCode,
    ) {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.loadChannels(country, language)
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
