package fho.kdvs.global.util

import fho.kdvs.global.database.BroadcastEntity

object URLs {
    const val LIVE_MP3 = "http://archives.kdvs.org:8000/kdvs128mp3"
    const val LIVE_AAC = "http://archives.kdvs.org:8000/kdvs32ogg"
    const val LIVE_OGG = "http://archives.kdvs.org:8000/stream"

    /** A list of all KDVS live URLs. */
    val liveStreamUrls = listOf(LIVE_AAC, LIVE_MP3, LIVE_OGG)

    const val SCHEDULE = "https://kdvs.org/programming/schedule-grid/"

    const val NEWS = "https://kdvs.org/category/kdvs-news/"

    const val CONTACT = "https://kdvs.org/about/contact/"

    const val SHOW_IMAGE_PLACEHOLDER = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg"

    const val TOP_ADDS = "https://kdvs.org/programming/top-5-adds/"

    const val TOP_ALBUMS = "https://kdvs.org/programming/top-30/"

    const val FUNDRAISER = "https://fundraiser.kdvs.org/"

    const val DISCOGS_SEARCH_URL = "https://www.discogs.com/search/?q="

    const val DISCOGS_QUERYSTRING = "&type=all"

    const val YOUTUBE_SEARCH_URL = "https://www.youtube.com/results?search_query="

    const val YOUTUBE_QUERYSTRING = "&sp=EgIQAQ%253D%253D"

    const val CONTACT_EMAIL = "kdvsappdevs@gmail.com"

    fun showDetails(id: String) = "https://kdvs.org/past-playlists/$id/"

    fun broadcastDetails(id: String) = "https://kdvs.org/playlist-details/$id/"

    fun archiveForBroadcast(broadcast: BroadcastEntity): String? {
        val dateString = broadcast.date?.let { TimeHelper.dateFormatter.format(it) } ?: return null
        return "https://archives.kdvs.org/archives/${dateString}_${broadcast.showId}_320kbps.mp3"
    }
}
