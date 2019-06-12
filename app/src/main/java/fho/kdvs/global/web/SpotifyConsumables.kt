package fho.kdvs.global.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpotifyData(
    val href: String? = null,
    val items: List<SpotifyItem>? = null
)

inline val SpotifyData?.album: String?
    get() = this?.items?.firstOrNull()?.name

inline val SpotifyData?.imageHref: String?
    get() = this?.items?.firstOrNull()?.images?.firstOrNull()?.url

inline val SpotifyData?.uri: String?
    get() = this?.items?.firstOrNull()?.uri

inline val SpotifyData?.year: Int?
    get() = this?.items?.firstOrNull()?.releaseDate?.let {
                if (it.length >= 4) {
                    it.substring(0, 3).toIntOrNull()
                } else {
                    null
                }
            }

@Serializable
data class SpotifyItem(
    val images: List<SpotifyImage>? = null,
    @SerialName("available_markets")
    val availableMarkets: List<String>? = null,
    @SerialName("release_date_precision")
    val releaseDatePrecision: String? = null,
    val type: String? = null,
    val uri: String? = null,
    @SerialName("total_tracks")
    val totalTracks: String? = null,
    val artists: List<SpotifyArtist>? = null,
    @SerialName("release_date")
    val releaseDate: String? = null,
    val name: String? = null,
    @SerialName("album_type")
    val albumType: String? = null,
    val href: String? = null,
    val id: String? = null,
    @SerialName("external_urls")
    val externalUrls: List<SpotifyExternalUrl>? = null
)

@Serializable
data class SpotifyArtist(
    val name: String? = null,
    val href: String? = null,
    val id: String? = null,
    val type: String? = null,
    @SerialName("external_urls")
    val externalUrls: List<SpotifyExternalUrl>? = null,
    val uri: String? = null
)

@Serializable
data class SpotifyExternalUrl(
    val spotify: String? = null
)

@Serializable
data class SpotifyImage(
    val width: String? = null,
    val url: String? = null,
    val height: String? = null
)