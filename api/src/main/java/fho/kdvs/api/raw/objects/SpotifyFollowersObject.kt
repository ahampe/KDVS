package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName

data class SpotifyFollowersObject(
    @SerializedName("href") val href: String,
    @SerializedName("total") val total: Int
)