package fho.kdvs.services

enum class PlaybackType(val type: String) {
    LIVE("LIVE"),
    ARCHIVE("ARCHIVE")
}

object PlaybackTypeHelper {
    // TODO: make this less tightly coupled to player tag / metadata description
    fun getPlaybackTypeFromTag(tag: String): PlaybackType? {
        val liveRegex = "^Live"
            .toRegex()
        val archiveRegex = "^\\w{3} \\d{2}, \\d{4}"
            .toRegex()

        return if (liveRegex.containsMatchIn(tag)) {
            PlaybackType.LIVE
        } else if (archiveRegex.containsMatchIn(tag)) {
            PlaybackType.ARCHIVE
        } else null
    }
}