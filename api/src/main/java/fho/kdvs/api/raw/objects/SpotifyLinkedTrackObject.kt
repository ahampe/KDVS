package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class SpotifyLinkedTrackObject (
    @SerializedName("external_urls") val external_urls: JSONObject,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String
)