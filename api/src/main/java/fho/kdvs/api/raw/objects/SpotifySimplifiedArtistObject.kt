package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class SpotifySimplifiedArtistObject (
    @SerializedName("external_urls") val external_urls: Map<String, String>,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String
)