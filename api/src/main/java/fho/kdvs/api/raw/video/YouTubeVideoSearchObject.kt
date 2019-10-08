package fho.kdvs.api.raw.video

import com.google.gson.annotations.SerializedName

data class YouTubeVideoSearchObject (
    @SerializedName("id") val id: YouTubeVideoObject
)