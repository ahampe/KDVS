package fho.kdvs.util

object URLs {
    const val SCHEDULE = "https://kdvs.org/programming/schedule-grid/"

    fun showDetails(id: String) = "https://kdvs.org/past-playlists/$id/"

    fun broadcastDetails(id: String) = "https://kdvs.org/playlist-details/$id/"
}