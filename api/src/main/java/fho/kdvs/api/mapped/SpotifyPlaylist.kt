package fho.kdvs.api.mapped

data class SpotifyPlaylist (
    val uri: String,
    val id: String,
    val name: String,
    val count: Int = 0
)