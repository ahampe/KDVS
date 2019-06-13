package fho.kdvs.global.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoverArtArchiveData(
    val images: List<CoverArtArchiveImage>? = null,
    val release: String
)

@Serializable
data class MusicBrainzRecordingData(
    val offset: String? = null,
    val recordings: List<Recording>? = null,
    val created: String? = null,
    val count: String? = null
)

@Serializable
data class MusicBrainzReleaseData(
    val offset: String? = null,
    val releases: List<Release>? = null,
    val created: String? = null,
    val count: String? = null
)

inline val MusicBrainzReleaseData?.album: String?
    get() = this?.releases?.firstOrNull()?.title

inline val MusicBrainzReleaseData?.id: String?
    get() = this?.releases?.firstOrNull()?.id

inline val MusicBrainzReleaseData?.label: String?
    get() = this?.releases?.firstOrNull()?.labelInfo?.label?.name

inline val MusicBrainzReleaseData?.year: Int?
    get() = this?.releases?.firstOrNull()?.date?.toIntOrNull()

@Serializable
data class CoverArtArchiveImage(
    val image: String? = null,
    val types: List<String>? = null,
    val approved: String? = null,
    val edit: String? = null,
    val back: String? = null,
    val comment: String? = null,
    val front: String? = null,
    val id: String? = null,
    val thumbnails: List<Thumbnail>? = null
)

@Serializable
data class Thumbnail(
    val small: String? = null,
    val large: String? = null,
    @SerialName("500")
    val xxl: String? = null,
    @SerialName("1200")
    val xxxl: String? = null,
    @SerialName("250")
    val xl: String? = null
)

@Serializable
data class Alias(
    @SerialName("begin-date")
    val beginDate: String? = null,
    @SerialName("end-date")
    val endDate: String? = null,
    val name: String? = null,
    @SerialName("sort-name")
    val sortName: String? = null,
    val locale: String? = null,
    val type: String? = null,
    val primary: String? = null
)

@Serializable
data class Area(
    @SerialName("iso-3166-1-codes")
    val isoCodes: List<String>? = null,
    val name: String? = null,
    val id: String? = null,
    @SerialName("sort-name")
    val sortName: String? = null
)

@Serializable
data class Artist(
    val aliases: List<Alias>? = null,
    val name: String? = null,
    val disambiguation: String? = null,
    val id: String? = null,
    @SerialName("sort-name")
    val sortName: String? = null
)

@Serializable
data class ArtistCredit(
    val artist: Artist? = null
)

@Serializable
data class Label(
    val name: String? = null,
    val id: String? = null
)

@Serializable
data class LabelInfo(
    val label: Label? = null,
    @SerialName("catalog-number")
    val catalogNumber: String? = null
)

@Serializable
data class Media(
    val format: String? = null,
    val position: String? = null,
    @SerialName("track-offset")
    val trackOffset: String? = null,
    val track: List<Track>? = null,
    @SerialName("track-count")
    val trackCount: String? = null,
    @SerialName("disc-count")
    val discCount: String? = null
)

@Serializable
data class Recording(
    val score: String? = null,
    val length: String? = null,
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit>? = null,
    val id: String? = null,
    val video: String? = null,
    val title: String? = null,
    val releases: List<Release>? = null
)

@Serializable
data class ReleaseEvent(
    val date: String? = null,
    val area: Area? = null
)

@Serializable
data class ReleaseGroup(
    @SerialName("primary-type")
    val primaryType: String? = null,
    @SerialName("secondary-types")
    val secondaryTypes: List<String>? = null,
    @SerialName("type-id")
    val typeId: String? = null,
    val id: String? = null,
    val title: String? = null
)

@Serializable
data class Release(
    @SerialName("text-representation")
    val textRepresentation: TextRepresentation? = null,
    @SerialName("release-group")
    val releaseGroup: ReleaseGroup? = null,
    val date: String? = null,
    val country: String? = null,
    @SerialName("release-events")
    val releaseEvents: List<ReleaseEvent>? = null,
    val count: String? = null,
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit>? = null,
    val score: String? = null,
    val id: String? = null,
    val media: List<Media>? = null,
    val title: String? = null,
    @SerialName("track-count")
    val trackCount: String? = null,
    val status: String? = null,
    @SerialName("label-info")
    val labelInfo: LabelInfo? = null
)

@Serializable
data class Tag(
    val count: String? = null,
    val name: String? = null
)

@Serializable
data class TextRepresentation(
    val language: String? = null,
    val script: String? = null
)

@Serializable
data class Track(
    val number: String? = null,
    val length: String? = null,
    val id: String? = null,
    val title: String? = null
)