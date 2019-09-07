package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName

data class MusicBrainzAlbumsResponse(
    @SerializedName("releases") val releases: List<MusicBrainzReleaseResponse>
)
