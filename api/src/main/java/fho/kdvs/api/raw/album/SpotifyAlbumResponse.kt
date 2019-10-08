package fho.kdvs.api.raw.album

import com.google.gson.annotations.SerializedName
import fho.kdvs.api.raw.SpotifyImageResponse
import fho.kdvs.api.raw.objects.SpotifyPager
import fho.kdvs.api.raw.objects.SpotifySimplifiedTrackObject

data class SpotifyAlbumResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<SpotifyImageResponse>,
    @SerializedName("uri") val uri: String,
    @SerializedName("release_date") val releaseDate: String,
    @SerializedName("tracks") val tracks: SpotifyPager<SpotifySimplifiedTrackObject>?
)
