package fho.kdvs.api.raw.track

import com.google.gson.annotations.SerializedName
import fho.kdvs.api.raw.objects.SpotifySimplifiedAlbumObject
import fho.kdvs.api.raw.objects.SpotifySimplifiedArtistObject

data class SpotifyTrackResponse (
    @SerializedName("album") val album: SpotifySimplifiedAlbumObject,
    @SerializedName("artists") val artists: List<SpotifySimplifiedArtistObject>,
    @SerializedName("name") val name: String,
    @SerializedName("href") val href: String,
    @SerializedName("id") val id: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("release_date") val releaseDate: String
)