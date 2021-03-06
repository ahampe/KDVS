package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class SpotifySimplifiedAlbumObject (
    @SerializedName("album_group") val album_group: String?,
    @SerializedName("album_type") val album_type: String,
    @SerializedName("artists") val artists: List<SpotifySimplifiedArtistObject>,
    @SerializedName("available_markets") val available_markets: List<String>,
    @SerializedName("external_urls") val external_urls: Map<String, String>,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("images") val images: List<SpotifyImageObject>,
    @SerializedName("name") val name: String,
    @SerializedName("release_date") val release_date: String,
    @SerializedName("release_date_precision") val release_date_precision: String,
    @SerializedName("restrictions") val restrictions: JSONObject,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String
)