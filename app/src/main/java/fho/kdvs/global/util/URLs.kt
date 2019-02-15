package fho.kdvs.global.util

import fho.kdvs.global.database.BroadcastEntity

object URLs {
    const val LIVE_MP3 = "http://archives.kdvs.org:8000/kdvs128mp3"
    const val LIVE_AAC = "http://archives.kdvs.org:8000/kdvs32ogg"
    const val LIVE_OGG = "http://archives.kdvs.org:8000/stream"

    val liveStream = listOf(LIVE_AAC, LIVE_MP3, LIVE_OGG)

    const val SCHEDULE = "https://kdvs.org/programming/schedule-grid/"

    fun showDetails(id: String) = "https://kdvs.org/past-playlists/$id/"

    fun broadcastDetails(id: String) = "https://kdvs.org/playlist-details/$id/"

    fun playlistForBroadcast(broadcast: BroadcastEntity): String? {
        val dateString = broadcast.date?.let { TimeHelper.dateFormatter.format(it) } ?: return null
        return "https://archives.kdvs.org/archives/${dateString}_${broadcast.showId}_320kbps.mp3"
    }

    // TODO temp
    const val WFMU = "http://stream0.wfmu.org/freeform-128k"
    const val WMNF = "https://stream.wmnf.org:4443/wmnf_high_quality"
}