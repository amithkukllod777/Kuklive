package com.kuklive.app.data

import com.kuklive.app.data.model.Channel

/**
 * Small, hand-picked playlists of reliable free-to-air streams, bundled in the
 * app so they always appear at the top of the list. These are direct HLS
 * streams sourced from the curated Free-TV project (mostly news, which is what's
 * legally free). Logos are left blank to avoid broken images.
 *
 * Tweak this list as streams change.
 */
object CuratedPlaylists {

    fun forCountry(code: String): List<Channel> = when (code.trim().lowercase()) {
        "in" -> M3UParser.parse(INDIA)
        else -> emptyList()
    }

    private val INDIA = """
        #EXTM3U
        #EXTINF:-1 tvg-id="AajTak.in" tvg-country="IN" group-title="News",Aaj Tak
        https://feeds.intoday.in/aajtak/api/aajtakhd/master.m3u8
        #EXTINF:-1 tvg-id="IndiaToday.in" tvg-country="IN" group-title="News",India Today
        https://indiatodaylive.akamaized.net/hls/live/2014320/indiatoday/indiatodaylive/playlist.m3u8
        #EXTINF:-1 tvg-id="NDTVIndia.in" tvg-country="IN" group-title="News",NDTV India
        https://ndtvindiaelemarchana.akamaized.net/hls/live/2003679/ndtvindia/master.m3u8
        #EXTINF:-1 tvg-id="ABPNews.in" tvg-country="IN" group-title="News",ABP News
        https://abplivetv.pc.cdn.bitgravity.com/httppush/abp_livetv/abp_abpnews/master.m3u8
        #EXTINF:-1 tvg-id="ABPAnanda.in" tvg-country="IN" group-title="News",ABP Ananda
        https://abplivetv.pc.cdn.bitgravity.com/httppush/abp_livetv/abp_ananda/master.m3u8
    """.trimIndent()
}
