package com.kuklive.app.data.model

/**
 * A single playable IPTV channel parsed from an M3U playlist, enriched with
 * category / country / language metadata used by the filter bar.
 */
data class Channel(
    val id: String,
    val name: String,
    val url: String,
    val logoUrl: String? = null,
    val categories: List<String> = emptyList(),
    val country: String? = null,
    val languages: List<String> = emptyList(),
) {
    val primaryCategory: String
        get() = categories.firstOrNull()?.takeIf { it.isNotBlank() } ?: "Uncategorized"
}
