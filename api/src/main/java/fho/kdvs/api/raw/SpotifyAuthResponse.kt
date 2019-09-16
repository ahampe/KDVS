package fho.kdvs.api.raw

import com.google.gson.annotations.SerializedName

data class SpotifyAuthResponse (
    @SerializedName("access_token") val token: String
)
