package fho.kdvs.api.raw.track

import com.google.gson.annotations.SerializedName

data class SpotifyTracksResponse (
    @SerializedName("tracks") val tracks: SpotifyTrackItemsResponse
)