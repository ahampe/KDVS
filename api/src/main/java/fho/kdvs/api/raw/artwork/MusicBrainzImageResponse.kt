package fho.kdvs.api.raw.artwork

import com.google.gson.annotations.SerializedName

data class MusicBrainzImageResponse(
    @SerializedName("image") val imageHref: String?
)
