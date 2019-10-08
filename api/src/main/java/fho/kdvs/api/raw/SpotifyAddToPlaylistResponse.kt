package fho.kdvs.api.raw

import com.google.gson.annotations.SerializedName

data class SpotifyAddToPlaylistResponse (
    @SerializedName("snapshot_id") val snapshot_id: String
)