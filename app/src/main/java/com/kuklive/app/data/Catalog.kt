package com.kuklive.app.data

/**
 * Static pickers for the setup screen. Codes match iptv-org's per-country
 * (ISO 3166-1 alpha-2) and per-language (ISO 639-3) playlist file names, e.g.
 * `countries/in.m3u` and `languages/hin.m3u`.
 */
object Catalog {

    data class Country(val code: String, val name: String) {
        val flag: String get() = CountryUtil.flag(code) ?: ""
        val label: String get() = (flag.ifEmpty { "" }.let { if (it.isEmpty()) name else "$it  $name" })
    }

    data class Language(val code: String, val name: String)

    val countries: List<Country> = listOf(
        Country("in", "India"),
        Country("pk", "Pakistan"),
        Country("bd", "Bangladesh"),
        Country("np", "Nepal"),
        Country("lk", "Sri Lanka"),
        Country("us", "United States"),
        Country("gb", "United Kingdom"),
        Country("ca", "Canada"),
        Country("au", "Australia"),
        Country("ae", "United Arab Emirates"),
        Country("sa", "Saudi Arabia"),
        Country("qa", "Qatar"),
        Country("de", "Germany"),
        Country("fr", "France"),
        Country("es", "Spain"),
        Country("it", "Italy"),
        Country("pt", "Portugal"),
        Country("nl", "Netherlands"),
        Country("br", "Brazil"),
        Country("mx", "Mexico"),
        Country("ar", "Argentina"),
        Country("ru", "Russia"),
        Country("tr", "Turkey"),
        Country("cn", "China"),
        Country("jp", "Japan"),
        Country("kr", "South Korea"),
        Country("id", "Indonesia"),
        Country("my", "Malaysia"),
        Country("sg", "Singapore"),
        Country("th", "Thailand"),
        Country("ph", "Philippines"),
        Country("vn", "Vietnam"),
        Country("za", "South Africa"),
        Country("ng", "Nigeria"),
        Country("eg", "Egypt"),
        Country("ke", "Kenya"),
        Country("ir", "Iran"),
        Country("il", "Israel"),
        Country("ua", "Ukraine"),
        Country("pl", "Poland"),
        Country("gr", "Greece"),
    )

    /** Empty code means "any language" (no language filter). */
    val languages: List<Language> = listOf(
        Language("", "Any language"),
        Language("hin", "Hindi"),
        Language("eng", "English"),
        Language("tam", "Tamil"),
        Language("tel", "Telugu"),
        Language("ben", "Bengali"),
        Language("mar", "Marathi"),
        Language("kan", "Kannada"),
        Language("mal", "Malayalam"),
        Language("guj", "Gujarati"),
        Language("pan", "Punjabi"),
        Language("urd", "Urdu"),
        Language("asm", "Assamese"),
        Language("spa", "Spanish"),
        Language("ara", "Arabic"),
        Language("fra", "French"),
        Language("por", "Portuguese"),
        Language("rus", "Russian"),
        Language("deu", "German"),
        Language("ita", "Italian"),
        Language("tur", "Turkish"),
        Language("zho", "Chinese"),
        Language("jpn", "Japanese"),
        Language("kor", "Korean"),
        Language("ind", "Indonesian"),
        Language("msa", "Malay"),
        Language("tha", "Thai"),
        Language("vie", "Vietnamese"),
        Language("fas", "Persian"),
        Language("nld", "Dutch"),
    )

    fun countryName(code: String?): String? =
        code?.let { c -> countries.firstOrNull { it.code.equals(c, true) }?.name ?: CountryUtil.name(c) }

    fun countryLabel(code: String?): String? {
        if (code.isNullOrBlank()) return null
        val flag = CountryUtil.flag(code) ?: ""
        val name = countryName(code) ?: code.uppercase()
        return if (flag.isEmpty()) name else "$flag  $name"
    }

    fun languageName(code: String?): String? =
        if (code.isNullOrBlank()) null else languages.firstOrNull { it.code == code }?.name ?: code
}
