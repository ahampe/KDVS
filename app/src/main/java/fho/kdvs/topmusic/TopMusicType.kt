package fho.kdvs.topmusic

enum class TopMusicType {
    ADD, ALBUM
}

inline val TopMusicType.limit
    get() = when (this) {
        TopMusicType.ADD -> 5
        TopMusicType.ALBUM -> 30
    }