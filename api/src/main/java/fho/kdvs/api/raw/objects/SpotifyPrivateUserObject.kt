package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class SpotifyPrivateUserObject (
    @SerializedName("country") val country: String,
    @SerializedName("display_name") val display_name: String,
    @SerializedName("email") val email: String,
    @SerializedName("external_urls") val external_urls: JSONObject,
    @SerializedName("followers") val followers: SpotifyFollowersObject,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("images") val images: Array<SpotifyImageObject>,
    @SerializedName("product") val product: String,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String
)