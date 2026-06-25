package com.kuklive.app.data.model

/**
 * A single playable IPTV channel parsed from an M3U playlist.
 */
data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val group: String? = null,
) {
    val groupOrDefault: String
        get() = group?.takeIf { it.isNotBlank() } ?: "Uncategorized"
}
