package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName

data class SpotifyPlaylistTrackObject (
    @SerializedName("added_at") val added_at: String?,
    @SerializedName("added_by") val added_by: SpotifyPublicUserObject?,
    @SerializedName("is_local") val is_local: Boolean,
    @SerializedName("track") val track: SpotifyTrackObject
)