package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName

data class MusicBrainzLabel(
    @SerializedName("name") val name: String?
)
