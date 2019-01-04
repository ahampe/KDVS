package fho.kdvs.playback

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import timber.log.Timber

/** A class that manages [AudioFocusRequest]s.
 * The FocusManager knows nothing about the [RadioMediaPlayer]. Use a [focusListener] to set the player status as audio focus changes.
 * The media player should ensure that [isFocusGranted] returns true in response to the UI requesting playback.
 * Whenever playback is stopped or paused, [abandonFocus] should be called. */
class FocusManager(context: Context, private val focusListener: PlaybackFocusListener) {

    // region member fields / properties
    private val audioMgr: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val focusLock = Any()
    private var playbackDelayed = false
    private var resumeOnFocusGain = false

    private val listener = AudioManager.OnAudioFocusChangeListener {
        when (it) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                Timber.d("AF gain")
                if (playbackDelayed || resumeOnFocusGain) {
                    synchronized(focusLock) {
                        playbackDelayed = false
                        resumeOnFocusGain = false
                    }
                    focusListener.onGainedAudioFocus()
                }
            }
            AudioManager.AUDIOFOCUS_LOSS -> {
                Timber.d("AF loss")
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = false
                }
                focusListener.onLostAudioFocus()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Timber.d("AF loss transient")
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = true
                }
                focusListener.onLostAudioFocusTransient()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                Timber.d("AF loss transient can duck")
                synchronized(focusLock) {
                    playbackDelayed = false
                    resumeOnFocusGain = true
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                    focusListener.onLostAudioFocusCanDuck()
                }
            }
        }
    }

    private val focusRequest =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(AudioHelper.attrs)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(listener)
                .build()
        } else {
            null
        }
    // endregion

    // region public API
    /** Makes an [AudioFocusRequest] and returns Boolean indicating whether request was granted. */
    val isFocusGranted: Boolean
        get() {
            val req = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioMgr.requestAudioFocus(focusRequest!!)
            } else {
                audioMgr.requestAudioFocus(listener, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            }

            synchronized(focusLock) {
                return when (req) {
                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> true
                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                        playbackDelayed = true
                        false
                    }
                    else -> false
                }
            }
        }

    /** Makes an abandon [AudioFocusRequest]. Returns nothing. */
    fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioMgr.abandonAudioFocusRequest(focusRequest!!)
        } else {
            audioMgr.abandonAudioFocus(listener)
        }
    }
    // endregion
}