package fho.kdvs.database.models

data class Track(
    val broadcastId: Int?,
    val position: Int?,
    val artist: String?,
    val song: String?,
    val album: String?,
    val label: String?,
    val comment: String?,
    val airbreak: Boolean? = false
)
