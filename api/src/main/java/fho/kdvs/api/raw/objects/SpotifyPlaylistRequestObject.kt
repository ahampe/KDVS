package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName

data class SpotifyPlaylistRequestObject (
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String
)