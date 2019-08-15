package fho.kdvs.services

import android.content.Context
import fho.kdvs.R

enum class PlaybackType(val type: String) {
    LIVE("LIVE"),
    ARCHIVE("ARCHIVE")
}

object PlaybackTypeHelper {
    // TODO: make this less tightly coupled to player tag / metadata description
    fun getPlaybackTypeFromTag(tag: String, context: Context): PlaybackType? {
        val liveRegex = (context.resources.getString(R.string.notification_title_separator) +
                context.resources.getString(R.string.live))
            .toRegex()
        val archiveRegex = (context.resources.getString(R.string.notification_title_separator) +
                "\\w{3} \\d{2}, \\d{4}")
            .toRegex()

        return when {
            liveRegex.containsMatchIn(tag) -> PlaybackType.LIVE
            archiveRegex.containsMatchIn(tag) -> PlaybackType.ARCHIVE
            else -> null
        }
    }
}