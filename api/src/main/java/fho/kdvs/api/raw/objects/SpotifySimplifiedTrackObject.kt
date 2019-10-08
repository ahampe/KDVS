package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class SpotifySimplifiedTrackObject (
    @SerializedName("artists") val artists: List<SpotifySimplifiedArtistObject>,
    @SerializedName("available_markets") val available_markets: List<String>,
    @SerializedName("disc_number") val disc_number: Int,
    @SerializedName("duration_ms") val duration_ms: Long,
    @SerializedName("explicit") val explicit: Boolean,
    @SerializedName("external_urls") val external_urls: Map<String, String>,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String
)