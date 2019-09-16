package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName
import fho.kdvs.api.raw.SpotifyImageResponse

data class SpotifyAlbumResponse(
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<SpotifyImageResponse>,
    @SerializedName("uri") val uri: String,
    @SerializedName("release_date") val releaseDate: String
)
