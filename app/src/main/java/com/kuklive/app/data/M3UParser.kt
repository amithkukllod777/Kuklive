package com.kuklive.app.data

import com.kuklive.app.data.model.Channel

/**
 * Minimal, dependency-free parser for extended M3U (#EXTM3U) playlists, the
 * de-facto format used by every IPTV provider.
 *
 * Example entry:
 * ```
 * #EXTINF:-1 tvg-id="x" tvg-logo="http://logo.png" group-title="News",BBC News
 * http://stream.example/bbc.m3u8
 * ```
 */
object M3UParser {

    private val attrRegex = Regex("""([\w-]+)="([^"]*)"""")

    fun parse(content: String): List<Channel> {
        val channels = mutableListOf<Channel>()
        val lines = content.lineSequence().iterator()

        var pendingName: String? = null
        var pendingLogo: String? = null
        var pendingGroup: String? = null
        var index = 0

        while (lines.hasNext()) {
            val raw = lines.next().trim()
            if (raw.isEmpty()) continue

            when {
                raw.startsWith("#EXTM3U") -> {
                    // Header line — ignore.
                }

                raw.startsWith("#EXTINF") -> {
                    val attrs = attrRegex.findAll(raw).associate { it.groupValues[1] to it.groupValues[2] }
                    pendingLogo = attrs["tvg-logo"]?.takeIf { it.isNotBlank() }
                    pendingGroup = attrs["group-title"]?.takeIf { it.isNotBlank() }

                    // Display name is the text after the last comma on the line.
                    val comma = raw.lastIndexOf(',')
                    val displayName = if (comma != -1) raw.substring(comma + 1).trim() else ""
                    pendingName = displayName
                        .ifBlank { attrs["tvg-name"] }
                        ?.takeIf { it.isNotBlank() }
                }

                raw.startsWith("#") -> {
                    // Other directives (#EXTGRP, #EXTVLCOPT, etc.) — skip for now.
                }

                else -> {
                    // A non-comment line is a stream URL completing the previous #EXTINF.
                    val url = raw
                    if (url.startsWith("http", ignoreCase = true) || url.startsWith("rtmp", ignoreCase = true)) {
                        channels += Channel(
                            id = "${index++}_${url.hashCode()}",
                            name = pendingName?.takeIf { it.isNotBlank() } ?: "Channel ${channels.size + 1}",
                            url = url,
                            logoUrl = pendingLogo,
                            group = pendingGroup,
                        )
                    }
                    pendingName = null
                    pendingLogo = null
                    pendingGroup = null
                }
            }
        }
        return channels
    }
}
