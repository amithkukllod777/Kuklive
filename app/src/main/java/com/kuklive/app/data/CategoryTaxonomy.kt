package com.kuklive.app.data

/**
 * Normalises iptv-org's raw `group-title` categories into a curated,
 * industry-standard genre set (like Tata Play / JioTV / Airtel), with emoji
 * labels shown in a familiar order. Unknown categories fall through as
 * title-cased chips after the known genres so nothing is lost.
 */
object CategoryTaxonomy {

    // Curated genre labels in the order they should appear in the chip bar.
    const val ENTERTAINMENT = "🎭 Entertainment"
    const val MOVIES = "🎬 Movies"
    const val SERIES = "📺 Series"
    const val SPORTS = "🏏 Sports"
    const val NEWS = "📰 News"
    const val KIDS = "👶 Kids"
    const val MUSIC = "🎵 Music"
    const val DEVOTIONAL = "🙏 Devotional"
    const val EDUCATION = "🎓 Education"
    const val INFOTAINMENT = "📚 Infotainment"
    const val LIFESTYLE = "💃 Lifestyle"
    const val FOOD = "🍳 Food"
    const val TRAVEL = "✈️ Travel"
    const val CULTURE = "🎨 Art & Culture"
    const val BUSINESS = "💼 Business"
    const val SHOPPING = "🛍️ Shopping"
    const val AUTO = "🚗 Auto"
    const val WEATHER = "🌦️ Weather"
    const val POLITICS = "🏛️ Politics"

    private val order: List<String> = listOf(
        ENTERTAINMENT, MOVIES, SERIES, SPORTS, NEWS, KIDS, MUSIC, DEVOTIONAL,
        EDUCATION, INFOTAINMENT, LIFESTYLE, FOOD, TRAVEL, CULTURE, BUSINESS,
        SHOPPING, AUTO, WEATHER, POLITICS,
    )

    // iptv-org category (lowercase) -> curated genre.
    private val map: Map<String, String> = mapOf(
        "entertainment" to ENTERTAINMENT,
        "general" to ENTERTAINMENT,
        "comedy" to ENTERTAINMENT,
        "classic" to ENTERTAINMENT,
        "movies" to MOVIES,
        "series" to SERIES,
        "sports" to SPORTS,
        "news" to NEWS,
        "kids" to KIDS,
        "family" to KIDS,
        "animation" to KIDS,
        "music" to MUSIC,
        "religious" to DEVOTIONAL,
        "education" to EDUCATION,
        "science" to EDUCATION,
        "documentary" to INFOTAINMENT,
        "lifestyle" to LIFESTYLE,
        "relax" to LIFESTYLE,
        "cooking" to FOOD,
        "travel" to TRAVEL,
        "outdoor" to TRAVEL,
        "culture" to CULTURE,
        "business" to BUSINESS,
        "shop" to SHOPPING,
        "auto" to AUTO,
        "weather" to WEATHER,
        "legislative" to POLITICS,
    )

    /** Maps a raw category to a curated genre, or null for adult/empty. */
    fun genreFor(raw: String): String? {
        val key = raw.trim().lowercase()
        if (key.isEmpty() || key == "xxx" || key == "adult") return null
        return map[key] ?: raw.trim().replaceFirstChar { it.uppercase() }
    }

    /** Sort key: known genres in curated order, unknown ones after (alphabetical). */
    fun orderIndex(label: String): Int {
        val i = order.indexOf(label)
        return if (i >= 0) i else Int.MAX_VALUE
    }
}
