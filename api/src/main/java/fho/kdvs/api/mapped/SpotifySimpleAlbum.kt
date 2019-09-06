package fho.kdvs.api.mapped

data class SpotifySimpleAlbum (
    val id: String,
    val name: String,
    val uri: String,
    val year: Int?,
    val imageHref: String?
)
