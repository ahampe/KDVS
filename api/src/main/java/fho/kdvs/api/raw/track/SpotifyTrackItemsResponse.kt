package fho.kdvs.api.raw.track

import com.google.gson.annotations.SerializedName

data class SpotifyTrackItemsResponse (
    @SerializedName("items") val items: List<SpotifyTrackResponse>
)