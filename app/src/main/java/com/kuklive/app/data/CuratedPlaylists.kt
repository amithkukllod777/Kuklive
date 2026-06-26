package com.kuklive.app.data

import com.kuklive.app.data.model.Channel

/**
 * Small, hand-picked playlists of reliable free-to-air streams, bundled in the
 * app so they always appear at the top of the list. These are verified HLS
 * streams (mostly news, which is what's legally free); entertainment channels
 * like Star/Sony/Zee are paid and not available as free streams.
 *
 * Tweak this list as streams change — it's the curated baseline users see first.
 */
object CuratedPlaylists {

    fun forCountry(code: String): List<Channel> = when (code.trim().lowercase()) {
        "in" -> M3UParser.parse(INDIA)
        else -> emptyList()
    }

    private val INDIA = """
        #EXTM3U
        #EXTINF:-1 tvg-id="AajTak.in" tvg-country="IN" tvg-logo="https://i.imgur.com/eU6Jm0z.png" group-title="News",Aaj Tak
        https://feeds.intoday.in/aajtak/api/aajtakhd/master.m3u8
        #EXTINF:-1 tvg-id="IndiaToday.in" tvg-country="IN" tvg-logo="https://i.imgur.com/dnk6OQS.png" group-title="News",India Today
        https://indiatodaylive.akamaized.net/hls/live/2014320/indiatoday/indiatodaylive/playlist.m3u8
        #EXTINF:-1 tvg-id="NDTVIndia.in" tvg-country="IN" tvg-logo="https://i.imgur.com/QjJYohG.png" group-title="News",NDTV India
        https://ndtvindiaelemarchana.akamaized.net/hls/live/2003679/ndtvindia/master.m3u8
        #EXTINF:-1 tvg-id="NDTV247.in" tvg-country="IN" tvg-logo="https://i.imgur.com/0Dn0Z8C.png" group-title="News",NDTV 24x7
        https://ndtv24x7elemarchana.akamaized.net/hls/live/2003678/ndtv24x7/master.m3u8
        #EXTINF:-1 tvg-id="ABPNews.in" tvg-country="IN" tvg-logo="https://i.imgur.com/DKHUFVQ.png" group-title="News",ABP News
        https://abplivetv.pc.cdn.bitgravity.com/httppush/abp_livetv/abp_abpnews/master.m3u8
        #EXTINF:-1 tvg-id="ABPAnanda.in" tvg-country="IN" tvg-logo="https://i.imgur.com/MGMSee9.png" group-title="News",ABP Ananda
        https://abplivetv.pc.cdn.bitgravity.com/httppush/abp_livetv/abp_ananda/master.m3u8
        #EXTINF:-1 tvg-id="ABPMajha.in" tvg-country="IN" group-title="News",ABP Majha
        https://abplivetv.pc.cdn.bitgravity.com/httppush/abp_livetv/abp_majha/master.m3u8
        #EXTINF:-1 tvg-id="ABPAsmita.in" tvg-country="IN" group-title="News",ABP Asmita
        https://abplivetv.pc.cdn.bitgravity.com/httppush/abp_livetv/abp_asmita/master.m3u8
        #EXTINF:-1 tvg-id="GoodNewsToday.in" tvg-country="IN" group-title="News",Good News Today
        https://indiatodaylive.akamaized.net/hls/live/2014321/gnttv/gnttvlive/playlist.m3u8
    """.trimIndent()
}
