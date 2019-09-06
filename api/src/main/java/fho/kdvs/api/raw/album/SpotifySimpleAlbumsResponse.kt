package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName

data class SpotifySimpleAlbumsResponse(
    @SerializedName("albums") val albums: SpotifySimpleAlbumItemsResponse
)
