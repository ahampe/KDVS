package fho.kdvs.global.util

object URLs {
    const val LIVE_MP3 = "http://archives.kdvs.org:8000/kdvs128mp3"
    const val LIVE_AAC = "http://archives.kdvs.org:8000/kdvs32ogg"
    const val LIVE_OGG = "http://archives.kdvs.org:8000/stream"

    const val SCHEDULE = "https://kdvs.org/programming/schedule-grid/"

    fun showDetails(id: String) = "https://kdvs.org/past-playlists/$id/"

    fun broadcastDetails(id: String) = "https://kdvs.org/playlist-details/$id/"

    // TODO temp
    const val WFMU = "http://stream0.wfmu.org/freeform-128k"
    const val WMNF = "https://stream.wmnf.org:4443/wmnf_high_quality"
}