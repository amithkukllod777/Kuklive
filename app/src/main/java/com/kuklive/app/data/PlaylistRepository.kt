package com.kuklive.app.data

import com.kuklive.app.data.model.Channel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

/**
 * Loads channels from iptv-org's small per-country / per-language playlists so
 * only the subset the user picked is downloaded.
 *
 * Selection semantics (each set may hold multiple values):
 *  - countries + languages → channels in any chosen country AND any chosen language
 *  - countries only        → channels in any chosen country
 *  - languages only        → channels in any chosen language (across all countries)
 *  - neither               → the full index (everything)
 */
class PlaylistRepository(
    private val client: OkHttpClient = defaultClient(),
) {

    suspend fun loadChannels(countries: Set<String>, languages: Set<String>): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                coroutineScope {
                    val base = when {
                        countries.isNotEmpty() -> fetchMerged(countries.map { countryUrl(it) })
                        languages.isNotEmpty() -> fetchMerged(languages.map { languageUrl(it) })
                        else -> M3UParser.parse(fetch(FULL_INDEX))
                    }

                    val result = if (countries.isNotEmpty() && languages.isNotEmpty()) {
                        val langUrls = fetchUrlSet(languages.map { languageUrl(it) })
                        if (langUrls.isEmpty()) base else base.filter { it.url in langUrls }
                    } else {
                        base
                    }

                    if (result.isEmpty()) error("No channels found for this selection")
                    result
                }
            }
        }

    /** Fetches several playlists in parallel and merges them, de-duplicating by URL. */
    private suspend fun fetchMerged(urls: List<String>): List<Channel> = coroutineScope {
        val lists = urls.map { url ->
            async { runCatching { M3UParser.parse(fetch(url)) }.getOrDefault(emptyList()) }
        }.awaitAll()
        val seen = HashSet<String>()
        val merged = ArrayList<Channel>()
        for (list in lists) for (channel in list) if (seen.add(channel.url)) merged.add(channel)
        merged
    }

    /** Fetches several playlists in parallel and returns the union of their stream URLs. */
    private suspend fun fetchUrlSet(urls: List<String>): HashSet<String> = coroutineScope {
        val lists = urls.map { url ->
            async { runCatching { M3UParser.parse(fetch(url)).map { it.url } }.getOrDefault(emptyList()) }
        }.awaitAll()
        HashSet<String>().apply { lists.forEach { addAll(it) } }
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
