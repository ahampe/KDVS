package fho.kdvs.api.mapped

data class SpotifyAlbum (
    val id: String,
    val name: String,
    val uri: String,
    val year: Int?,
    val imageHref: String?,
    val tracks: List<SpotifyTrack?>?
)
