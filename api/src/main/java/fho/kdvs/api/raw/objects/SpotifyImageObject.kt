package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName

data class SpotifyImageObject(
    @SerializedName("height") val height: Int,
    @SerializedName("url") val url: String,
    @SerializedName("width") val width: Int
)
