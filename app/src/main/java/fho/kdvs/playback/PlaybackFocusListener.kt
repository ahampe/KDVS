package fho.kdvs.playback

interface PlaybackFocusListener {
    fun onGainedAudioFocus()
    fun onLostAudioFocus()
    fun onLostAudioFocusTransient()
    fun onLostAudioFocusCanDuck()
}