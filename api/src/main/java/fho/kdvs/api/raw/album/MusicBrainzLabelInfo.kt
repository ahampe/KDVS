package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName

data class MusicBrainzLabelInfo(
    @SerializedName("label") val label: MusicBrainzLabel?
)
