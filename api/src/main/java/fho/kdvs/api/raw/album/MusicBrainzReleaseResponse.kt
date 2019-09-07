package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName

data class MusicBrainzReleaseResponse(
    @SerializedName("id") val id: String?,
    @SerializedName("title") val title: String?,
    @SerializedName("date") val date: String?,
    @SerializedName("label-info") val labels: List<MusicBrainzLabelInfo>?
)
