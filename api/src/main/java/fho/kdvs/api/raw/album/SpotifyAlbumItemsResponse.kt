package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName

data class SpotifyAlbumItemsResponse(
    @SerializedName("items") val items: List<SpotifyAlbumResponse>
)
