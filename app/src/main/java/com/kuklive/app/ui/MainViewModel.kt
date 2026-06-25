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
    val isLoading: Boolean = true,
    val error: String? = null,
    val channels: List<Channel> = emptyList(),
    val favoriteUrls: Set<String> = emptySet(),
    val query: String = "",
    val selectedCategory: String? = null,
    val tab: Tab = Tab.CHANNELS,
    val playlistUrl: String = "",
) {
    val categories: List<String>
        get() = channels.map { it.groupOrDefault }.distinct().sorted()

    /** Channels after applying tab (favorites), category and search filters. */
    val visibleChannels: List<Channel>
        get() {
            val base = if (tab == Tab.FAVORITES) {
                channels.filter { it.url in favoriteUrls }
            } else {
                channels
            }
            return base
                .filter { selectedCategory == null || it.groupOrDefault == selectedCategory }
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

    /** The list and position handed to the player so it can zap up/down. */
    var playingChannels: List<Channel> = emptyList()
        private set
    var playingIndex: Int = 0
        private set

    init {
        // Keep favorites + playlist URL in sync with persisted settings.
        viewModelScope.launch {
            combine(settings.playlistUrl, settings.favoriteUrls) { url, favs -> url to favs }
                .collect { (url, favs) ->
                    val firstLoad = _state.value.playlistUrl.isEmpty()
                    _state.update { it.copy(playlistUrl = url, favoriteUrls = favs) }
                    if (firstLoad) reload()
                }
        }
    }

    fun reload() {
        val url = _state.value.playlistUrl.ifBlank { SettingsStore.DEFAULT_PLAYLIST_URL }
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.loadChannels(url)
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

    fun setPlaylistUrl(url: String) {
        viewModelScope.launch {
            settings.setPlaylistUrl(url)
            reload()
        }
    }

    /** Called when the user opens a channel; remembers the surrounding list for zapping. */
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
