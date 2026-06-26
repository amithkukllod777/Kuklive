package com.kuklive.app.data

import com.kuklive.app.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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

    suspend fun loadChannels(playlistUrl: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
            coroutineScope {
                // Kick off the optional language index in parallel with the main fetch.
                val languageJob = languageIndexUrl(playlistUrl)?.let { url ->
                    async { runCatching { M3UParser.parseGroupTitlesByUrl(fetch(url)) }.getOrNull() }
                }

                val channels = M3UParser.parse(fetch(playlistUrl))
                val languagesByUrl = languageJob?.await()

                if (languagesByUrl.isNullOrEmpty()) {
                    channels
                } else {
                    channels.map { channel ->
                        val langs = languagesByUrl[channel.url]
                        if (langs.isNullOrEmpty()) channel else channel.copy(languages = langs)
                    }
                }
            }
        }
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
