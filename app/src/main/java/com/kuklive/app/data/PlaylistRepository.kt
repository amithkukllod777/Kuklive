package com.kuklive.app.data

import com.kuklive.app.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Fetches and parses an M3U playlist. For iptv-org playlists it additionally
 * pulls the sibling `index.language.m3u` to enrich channels with language
 * metadata (the base playlist carries no language attribute).
 */
class PlaylistRepository(
    private val client: OkHttpClient = defaultClient(),
) {

    /** Fetches and parses the main playlist. Returns quickly so channels show fast. */
    suspend fun loadChannels(playlistUrl: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
            val channels = M3UParser.parse(fetch(playlistUrl))
            if (channels.isEmpty()) error("No channels found")
            channels
        }
    }

    /**
     * Best-effort language enrichment: downloads the (large) iptv-org language
     * index separately so it never blocks the initial channel list. Returns a
     * map of stream URL -> languages, or null when unavailable.
     */
    suspend fun loadLanguageMap(playlistUrl: String): Map<String, List<String>>? = withContext(Dispatchers.IO) {
        val langUrl = languageIndexUrl(playlistUrl) ?: return@withContext null
        runCatching { M3UParser.parseGroupTitlesByUrl(fetch(langUrl)) }.getOrNull()
    }

    private fun fetch(url: String): String {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "Kuklive/1.0")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("HTTP ${response.code} while fetching $url")
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) error("Empty response from $url")
            return body
        }
    }

    /** Derives the iptv-org language index URL that sits beside an index playlist. */
    private fun languageIndexUrl(playlistUrl: String): String? {
        if (!playlistUrl.contains("iptv-org.github.io/iptv")) return null
        if (playlistUrl.contains("index.language.m3u")) return null
        return playlistUrl.substringBeforeLast('/') + "/index.language.m3u"
    }

    companion object {
        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .build()
    }
}
