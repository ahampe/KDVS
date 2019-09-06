package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName

data class SpotifySimpleAlbumItemsResponse(
    @SerializedName("items") val items: List<SpotifySimpleAlbumResponse>
)
