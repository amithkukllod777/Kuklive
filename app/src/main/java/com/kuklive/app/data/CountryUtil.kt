package com.kuklive.app.data

import java.util.Locale

/**
 * Resolves ISO 3166-1 alpha-2 country codes (as found in iptv-org `tvg-id`
 * suffixes, e.g. `ABNTelugu.in`) into human-readable names and flag emoji.
 */
object CountryUtil {

    /** "in" -> "India". Returns null for codes Android can't resolve. */
    fun name(code: String): String? {
        val cc = code.trim().uppercase()
        if (cc.length != 2 || !cc.all { it in 'A'..'Z' }) return null
        val name = Locale("", cc).getDisplayCountry(Locale.ENGLISH)
        return name.takeIf { it.isNotBlank() && !it.equals(cc, ignoreCase = true) }
    }

    /** "in" -> "🇮🇳". Returns null for malformed codes. */
    fun flag(code: String): String? {
        val cc = code.trim().uppercase()
        if (cc.length != 2 || !cc.all { it in 'A'..'Z' }) return null
        val first = Character.toChars(0x1F1E6 + (cc[0] - 'A'))
        val second = Character.toChars(0x1F1E6 + (cc[1] - 'A'))
        return String(first) + String(second)
    }

    /** Combines flag + name for display, e.g. "🇮🇳 India". */
    fun display(code: String): String? {
        val n = name(code) ?: return null
        val f = flag(code)
        return if (f != null) "$f $n" else n
    }
}
