package fho.kdvs.playback

import android.media.AudioAttributes

/** A helper object that holds any static data needed for playback. */
internal object AudioHelper {
    val attrs: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()
}