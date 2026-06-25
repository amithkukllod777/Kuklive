package com.kuklive.app.data

import com.kuklive.app.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Fetches and parses an M3U playlist from a remote URL.
 */
class PlaylistRepository(
    private val client: OkHttpClient = defaultClient(),
) {

    suspend fun loadChannels(playlistUrl: String): Result<List<Channel>> = withContext(Dispatchers.IO) {
        runCatching {
            val request = Request.Builder()
                .url(playlistUrl)
                .header("User-Agent", "Kuklive/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("HTTP ${response.code} while fetching playlist")
                }
                val body = response.body?.string().orEmpty()
                if (body.isBlank()) error("Playlist is empty")
                M3UParser.parse(body)
            }
        }
    }

    companion object {
        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
