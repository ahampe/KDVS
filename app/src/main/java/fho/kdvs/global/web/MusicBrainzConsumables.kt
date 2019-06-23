package fho.kdvs.global.web

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CoverArtArchiveData(
    val images: List<CoverArtArchiveImage>?,
    val release: String
)

@Serializable
data class MusicBrainzRecordingData(
    val offset: String?,
    val recordings: List<Recording>?,
    val created: String?,
    val count: String?
)

@Serializable // TODO: this is causing parcel issues for some reason
data class MusicBrainzReleaseData(
    val offset: String?,
    val releases: List<Release>?,
    val created: String?,
    val count: String?
)

inline val MusicBrainzReleaseData?.album: String?
    get() = this?.releases?.firstOrNull()?.title

inline val MusicBrainzReleaseData?.id: String?
    get() = this?.releases?.firstOrNull()?.id

inline val MusicBrainzReleaseData?.label: String?
    get() = this?.releases?.firstOrNull()?.labelInfo?.firstOrNull()?.label?.name

inline val MusicBrainzReleaseData?.year: Int?
    get() = this?.releases?.firstOrNull()?.date?.toIntOrNull()

@Serializable
data class CoverArtArchiveImage(
    val image: String?,
    val types: List<String>?,
    val approved: String?,
    val edit: String?,
    val back: String?,
    val comment: String?,
    val front: String?,
    val id: String?,
    val thumbnails: Thumbnails?
)

@Serializable
data class Thumbnails(
    val small: String?,
    val large: String?,
    @SerialName("500")
    val xxl: String?,
    @SerialName("1200")
    val xxxl: String?,
    @SerialName("250")
    val xl: String?
)

@Serializable
data class Alias(
    @SerialName("begin-date")
    val beginDate: String?,
    @SerialName("end-date")
    val endDate: String?,
    val name: String?,
    @SerialName("sort-name")
    val sortName: String?,
    val locale: String?,
    val type: String?,
    val primary: String?
)

@Serializable
data class Area(
    @SerialName("iso-3166-1-codes")
    val isoCodes: List<String>?,
    val name: String?,
    val id: String?,
    @SerialName("sort-name")
    val sortName: String?
)

@Serializable
data class Artist(
    val aliases: List<Alias>?,
    val name: String?,
    val disambiguation: String?,
    val id: String?,
    @SerialName("sort-name")
    val sortName: String?
)

@Serializable
data class ArtistCredit(
    val artist: Artist?
)

@Serializable
data class Label(
    val name: String?,
    val id: String?
)

@Serializable
data class LabelInfo(
    val label: Label?,
    @SerialName("catalog-number")
    val catalogNumber: String?
)

@Serializable
data class Media(
    val format: String?,
    val position: String?,
    @SerialName("track-offset")
    val trackOffset: String?,
    val track: List<Track>?,
    @SerialName("track-count")
    val trackCount: String?,
    @SerialName("disc-count")
    val discCount: String?
)

@Serializable
data class Recording(
    val score: String?,
    val length: String?,
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit>?,
    val id: String?,
    val video: String?,
    val title: String?,
    val releases: List<Release>?
)

@Serializable
data class ReleaseEvent(
    val date: String?,
    val area: Area?
)

@Serializable
data class ReleaseGroup(
    @SerialName("primary-type")
    val primaryType: String?,
    @SerialName("secondary-types")
    val secondaryTypes: List<String>?,
    @SerialName("type-id")
    val typeId: String?,
    val id: String?,
    val title: String?
)

@Serializable
data class Release(
    @SerialName("text-representation")
    val textRepresentation: TextRepresentation?,
    @SerialName("release-group")
    val releaseGroup: ReleaseGroup?,
    val date: String?,
    val country: String?,
    @SerialName("release-events")
    val releaseEvents: List<ReleaseEvent>?,
    val count: String?,
    @SerialName("artist-credit")
    val artistCredit: List<ArtistCredit>?,
    val score: String?,
    val id: String?,
    val media: List<Media>?,
    val title: String?,
    @SerialName("track-count")
    val trackCount: String?,
    val status: String?,
    @SerialName("label-info")
    val labelInfo: List<LabelInfo>?
)

@Serializable
data class Tag(
    val count: String?,
    val name: String?
)

@Serializable
data class TextRepresentation(
    val language: String?,
    val script: String?
)

@Serializable
data class Track(
    val number: String?,
    val length: String?,
    val id: String?,
    val title: String?
)