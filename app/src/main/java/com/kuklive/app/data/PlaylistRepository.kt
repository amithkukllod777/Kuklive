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
 * Loads channels for the user's chosen countries / languages.
 *
 * Source strategy (for reliability):
 *  - Free-TV/IPTV — a small, actively-curated playlist of working streams —
 *    is loaded first so its channels appear at the top.
 *  - iptv-org's larger per-country / per-language playlists are merged in for
 *    breadth.
 *  - Non-playable links (e.g. YouTube live pages) are dropped.
 */
class PlaylistRepository(
    private val client: OkHttpClient = defaultClient(),
) {

    suspend fun loadChannels(
        countries: Set<String>,
        languages: Set<String>,
        customUrl: String = "",
    ): Result<List<Channel>> =
        withContext(Dispatchers.IO) {
            runCatching {
                // A custom playlist URL overrides everything — load it as-is.
                if (customUrl.isNotBlank()) {
                    val channels = M3UParser.parse(fetch(customUrl)).filter { isPlayable(it.url) }
                    if (channels.isEmpty()) error("No channels found in the custom playlist")
                    return@runCatching channels
                }
                coroutineScope {
                    val merged = when {
                        countries.isNotEmpty() -> {
                            // Bundled verified channels first, then Free-TV, then iptv-org.
                            val curated = countries.flatMap { CuratedPlaylists.forCountry(it) }
                            val freeTv = async { freeTvForCountries(countries) }
                            val iptvOrg = async { fetchMerged(countries.map { countryUrl(it) }) }
                            var iptvChannels = iptvOrg.await()
                            // Language filter narrows only iptv-org (curated/Free-TV have no
                            // language metadata, so their working channels always stay).
                            if (languages.isNotEmpty()) {
                                val langUrls = fetchUrlSet(languages.map { languageUrl(it) })
                                val narrowed = iptvChannels.filter { it.url in langUrls }
                                // Only narrow if it leaves a usable list; never wipe it out.
                                if (narrowed.isNotEmpty()) iptvChannels = narrowed
                            }
                            dedupe(curated + freeTv.await() + iptvChannels)
                        }
                        languages.isNotEmpty() -> fetchMerged(languages.map { languageUrl(it) })
                        else -> dedupe(M3UParser.parse(fetch(FREE_TV_MAIN)))
                    }

                    val playable = merged.filter { isPlayable(it.url) }
                    if (playable.isEmpty()) error("No channels found for this selection")
                    playable
                }
            }
        }

    /** Fetches the curated Free-TV playlist and keeps only the chosen countries. */
    private fun freeTvForCountries(countries: Set<String>): List<Channel> {
        val text = runCatching { fetch(FREE_TV_MAIN) }.getOrNull() ?: return emptyList()
        return M3UParser.parse(filterByCountry(text, countries))
    }

    /** Keeps only #EXTINF entries whose tvg-country is in [codes], with their URL line. */
    private fun filterByCountry(text: String, codes: Set<String>): String {
        val wanted = codes.map { it.trim().uppercase() }.toSet()
        val countryRegex = Regex("""tvg-country="([^"]*)"""", RegexOption.IGNORE_CASE)
        val lines = text.split('\n')
        val out = StringBuilder("#EXTM3U\n")
        var i = 0
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("#EXTINF")) {
                val cc = countryRegex.find(line)?.groupValues?.get(1)?.uppercase()
                if (cc != null && cc in wanted) {
                    out.append(line).append('\n')
                    var j = i + 1
                    while (j < lines.size && lines[j].trim().startsWith("#")) {
                        out.append(lines[j].trim()).append('\n'); j++
                    }
                    if (j < lines.size) out.append(lines[j].trim()).append('\n')
                    i = j
                }
            }
            i++
        }
        return out.toString()
    }

    private suspend fun fetchMerged(urls: List<String>): List<Channel> = coroutineScope {
        val lists = urls.map { url ->
            async { runCatching { M3UParser.parse(fetch(url)) }.getOrDefault(emptyList()) }
        }.awaitAll()
        dedupe(lists.flatten())
    }

    private suspend fun fetchUrlSet(urls: List<String>): HashSet<String> = coroutineScope {
        val lists = urls.map { url ->
            async { runCatching { M3UParser.parse(fetch(url)).map { it.url } }.getOrDefault(emptyList()) }
        }.awaitAll()
        HashSet<String>().apply { lists.forEach { addAll(it) } }
    }

    private fun dedupe(channels: List<Channel>): List<Channel> {
        val seen = HashSet<String>()
        val out = ArrayList<Channel>(channels.size)
        for (channel in channels) if (seen.add(channel.url)) out.add(channel)
        return out
    }

    /** ExoPlayer can't play YouTube pages or obvious non-stream links. */
    private fun isPlayable(url: String): Boolean {
        val u = url.lowercase()
        return !u.contains("youtube.com") && !u.contains("youtu.be")
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

    // Served from GitHub's raw host (more reliable on some ISPs than github.io).
    private fun countryUrl(code: String) =
        "https://raw.githubusercontent.com/iptv-org/iptv/gh-pages/countries/${code.trim().lowercase()}.m3u"

    private fun languageUrl(code: String) =
        "https://raw.githubusercontent.com/iptv-org/iptv/gh-pages/languages/${code.trim().lowercase()}.m3u"

    companion object {
        private const val FREE_TV_MAIN =
            "https://raw.githubusercontent.com/Free-TV/IPTV/master/playlist.m3u8"

        private fun defaultClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}
