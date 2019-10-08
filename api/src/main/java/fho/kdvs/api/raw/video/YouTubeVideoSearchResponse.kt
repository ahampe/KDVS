package fho.kdvs.api.raw.video

import com.google.gson.annotations.SerializedName

data class YouTubeVideoSearchResponse (
    @SerializedName("items") val items: List<YouTubeVideoSearchObject>
)