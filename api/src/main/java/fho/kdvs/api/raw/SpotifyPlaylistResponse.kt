package fho.kdvs.api.raw

import com.google.gson.annotations.SerializedName
import fho.kdvs.api.raw.objects.*

data class SpotifyPlaylistResponse (
    @SerializedName("collaborative") val collaborative: Boolean,
    @SerializedName("description") val description: String,
    @SerializedName("external_urls") val external_urls: SpotifyExternalURLObject,
    @SerializedName("followers") val followers: List<SpotifyFollowersObject>,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("images") val images: Array<SpotifyImageObject>,
    @SerializedName("name") val name: String,
    @SerializedName("owner") val owner: SpotifyPublicUserObject,
    @SerializedName("public") val public: Boolean?,
    @SerializedName("snapshot_id") val snapshot_id: String,
    @SerializedName("tracks") val tracks: List<SpotifyPlaylistTrackObject>,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String
)
