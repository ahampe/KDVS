package fho.kdvs.global.web

interface IThirdPartyMusicAPI {
    fun getMusicData(title: String?, artist: String?, type: ThirdPartyQueryType): ThirdPartyData?
}

abstract class ThirdPartyData {
    abstract val albumTitle: String?
    abstract val imageHref: String?
    abstract val year: Int?
}

enum class ThirdPartyQueryType {
    SONG,
    ALBUM
}