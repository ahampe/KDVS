package fho.kdvs.api.raw

import com.google.gson.annotations.SerializedName
import fho.kdvs.api.raw.objects.SpotifyTracksObject

data class SpotifyPlaylistResponse (
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("tracks") val tracks: SpotifyTracksObject?
)
