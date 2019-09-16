package fho.kdvs.api.raw

import com.google.gson.annotations.SerializedName

data class SpotifyImageResponse(
    @SerializedName("url") val url: String
)
