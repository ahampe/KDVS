package fho.kdvs.api.raw

import com.google.gson.annotations.SerializedName

data class SpotifyAuthRequest(
    @SerializedName("grant_type") val grantType: String = "client_credentials"
)
