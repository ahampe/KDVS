package fho.kdvs.api.raw.artwork

import com.google.gson.annotations.SerializedName

data class MusicBrainzAlbumArtResponse(
    @SerializedName("images") val images: List<MusicBrainzImageResponse>
)
