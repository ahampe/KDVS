package fho.kdvs.api.raw.video

import com.google.gson.annotations.SerializedName

data class YouTubeVideoObject (
    @SerializedName("kind") val kind: String,
    @SerializedName("videoId") val id: String
)