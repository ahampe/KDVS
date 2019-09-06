package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class SpotifyTrackObject (
    @SerializedName("album") val album: SpotifySimplifiedAlbumObject,
    @SerializedName("artists") val artists: Array<SpotifySimplifiedArtistObject>,
    @SerializedName("available_markets") val available_markets: Array<String>,
    @SerializedName("disc_number") val disc_number: Int,
    @SerializedName("duration_ms") val duration_ms: Int,
    @SerializedName("explicit") val explicit: Boolean,
    @SerializedName("external_ids") val external_ids: JSONObject,
    @SerializedName("external_urls") val external_urls: JSONObject,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("is_playable") val is_playable: Boolean,
    @SerializedName("linked_from") val linked_from: SpotifyLinkedTrackObject,
    @SerializedName("restrictions") val restrictions: JSONObject,
    @SerializedName("name") val name: String,
    @SerializedName("popularity") val popularity: Int,
    @SerializedName("preview_url") val preview_url: String,
    @SerializedName("track_number") val track_number: Int,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("is_local") val is_local: Boolean
)