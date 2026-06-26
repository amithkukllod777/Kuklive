package com.kuklive.app.data

import com.kuklive.app.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Loads channels from iptv-org's small per-country / per-language playlists so
 * only the subset the user picked is downloaded — far faster than the full
 * ~10k-channel index.
 */
class PlaylistRepository(
    private val client: OkHttpClient = defaultClient(),
) {

    suspend fun loadChannels(countryCode: String?, languageCode: String?): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                val hasCountry = !countryCode.isNullOrBlank()
                val hasLanguage = !languageCode.isNullOrBlank()

                // Base list: prefer the (usually smaller) country playlist.
                val baseUrl = when {
                    hasCountry -> countryUrl(countryCode!!)
                    hasLanguage -> languageUrl(languageCode!!)
                    else -> FULL_INDEX
                }
                var channels = M3UParser.parse(fetch(baseUrl))

                // When both are chosen, keep only channels also in the language list.
                if (hasCountry && hasLanguage) {
                    val langUrls = runCatching {
                        M3UParser.parse(fetch(languageUrl(languageCode!!))).map { it.url }.toHashSet()
                    }.getOrNull()
                    if (!langUrls.isNullOrEmpty()) {
                        channels = channels.filter { it.url in langUrls }
                    }
                }

                if (channels.isEmpty()) error("No channels found for this selection")
                channels
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

    private fun countryUrl(code: String) =
        "https://iptv-org.github.io/iptv/countries/${code.trim().lowercase()}.m3u"

    private fun languageUrl(code: String) =
        "https://iptv-org.github.io/iptv/languages/${code.trim().lowercase()}.m3u"

    companion object {
        private const val FULL_INDEX = "https://iptv-org.github.io/iptv/index.m3u"

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
