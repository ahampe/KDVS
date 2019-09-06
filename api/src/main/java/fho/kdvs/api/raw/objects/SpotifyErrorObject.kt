package fho.kdvs.api.raw.objects

import com.google.gson.annotations.SerializedName

data class SpotifyErrorObject(
    @SerializedName("status") val status: Int,
    @SerializedName("message") val message: String
)
