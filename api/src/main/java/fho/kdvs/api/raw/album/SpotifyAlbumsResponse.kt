package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName

data class SpotifyAlbumsResponse(
    @SerializedName("albums") val albums: SpotifyAlbumItemsResponse
)
