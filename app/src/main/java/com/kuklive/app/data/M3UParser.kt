package com.kuklive.app.data

import com.kuklive.app.data.model.Channel

/**
 * Minimal, dependency-free parser for extended M3U (#EXTM3U) playlists, the
 * de-facto format used by every IPTV provider.
 *
 * Beyond the raw stream URL it extracts:
 *  - a cleaned display name (quality / status tags stripped)
 *  - categories (split from a possibly multi-valued `group-title`)
 *  - country (from `tvg-country`, or the `tvg-id` suffix e.g. `BBCNews.uk`)
 *  - languages (from `tvg-language`, when present)
 */
object M3UParser {

    private val attrRegex = Regex("""([\w-]+)="([^"]*)"""")

    // " (1080p)", " (720p)", " (SD)", " (HD)", " (4K)" …
    private val qualityRegex = Regex("""\s*\((?:\d{3,4}[pi]|[248]K|U?HD|FHD|SD)\)""", RegexOption.IGNORE_CASE)

    // " [Not 24x7]", " [Geo-blocked]", any trailing bracket tag
    private val bracketRegex = Regex("""\s*\[[^\]]*]""")

    private val multiSpace = Regex("""\s{2,}""")

    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lineSequence().iterator()

        var attrs: Map<String, String> = emptyMap()
        var pendingName: String? = null
        var index = 0

        while (lines.hasNext()) {
            val raw = lines.next().trim()
            if (raw.isEmpty()) continue

            when {
                raw.startsWith("#EXTM3U") -> Unit

                raw.startsWith("#EXTINF") -> {
                    attrs = attrRegex.findAll(raw).associate { it.groupValues[1] to it.groupValues[2] }
                    val comma = raw.lastIndexOf(',')
                    val displayName = if (comma != -1) raw.substring(comma + 1).trim() else ""
                    pendingName = displayName.ifBlank { attrs["tvg-name"] }
                }

                raw.startsWith("#") -> Unit // #EXTGRP, #EXTVLCOPT, … — skip

                else -> {
                    val url = raw
                    if (url.startsWith("http", ignoreCase = true) || url.startsWith("rtmp", ignoreCase = true)) {
                        channels += Channel(
                            id = "${index++}_${url.hashCode()}",
                            name = cleanName(pendingName).ifBlank { "Channel ${channels.size + 1}" },
                            url = url,
                            logoUrl = attrs["tvg-logo"]?.takeIf { it.isNotBlank() },
                            categories = parseCategories(attrs["group-title"]),
                            country = resolveCountry(attrs),
                            languages = parseLanguages(attrs["tvg-language"]),
                        )
                    }
                    attrs = emptyMap()
                    pendingName = null
                }
            }
        }
        return channels
    }

    /**
     * Parses a "grouped index" playlist (e.g. iptv-org's `index.language.m3u`
     * or `index.country.m3u`) into a map of stream URL -> the set of
     * `group-title` values seen for it. Channels that belong to several groups
     * appear multiple times, so the values accumulate.
     */
    fun parseGroupTitlesByUrl(content: String): Map<String, List<String>> {
        val result = LinkedHashMap<String, LinkedHashSet<String>>()
        val lines = content.lineSequence().iterator()
        var group: String? = null

        while (lines.hasNext()) {
            val raw = lines.next().trim()
            if (raw.isEmpty()) continue
            when {
                raw.startsWith("#EXTINF") -> {
                    val attrs = attrRegex.findAll(raw).associate { it.groupValues[1] to it.groupValues[2] }
                    group = attrs["group-title"]?.takeIf { it.isNotBlank() }
                }
                raw.startsWith("#") || raw.startsWith("#EXTM3U") -> Unit
                else -> {
                    if (raw.startsWith("http", ignoreCase = true) || raw.startsWith("rtmp", ignoreCase = true)) {
                        group?.split(';')?.map { it.trim() }?.filter { it.isNotEmpty() }?.let { groups ->
                            val set = result.getOrPut(raw) { LinkedHashSet() }
                            set.addAll(groups)
                        }
                    }
                    group = null
                }
            }
        }
        return result.mapValues { it.value.toList() }
    }

    private fun parseCategories(groupTitle: String?): List<String> {
        if (groupTitle.isNullOrBlank()) return emptyList()
        return groupTitle.split(';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun parseLanguages(tvgLanguage: String?): List<String> {
        if (tvgLanguage.isNullOrBlank()) return emptyList()
        return tvgLanguage.split(';', ',')
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
    }

    private fun resolveCountry(attrs: Map<String, String>): String? {
        attrs["tvg-country"]?.takeIf { it.isNotBlank() }?.let { raw ->
            return if (raw.length == 2) CountryUtil.name(raw) ?: raw.uppercase() else raw
        }
        val suffix = attrs["tvg-id"]?.substringAfterLast('.', "")?.takeIf { it.length == 2 }
        return suffix?.let { CountryUtil.name(it) }
    }

    private fun cleanName(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        var s = raw
        s = bracketRegex.replace(s, "")
        s = qualityRegex.replace(s, "")
        s = multiSpace.replace(s, " ")
        return s.trim()
    }
}
