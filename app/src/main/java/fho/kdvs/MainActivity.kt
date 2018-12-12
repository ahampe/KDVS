package fho.kdvs

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.support.design.widget.BottomNavigationView
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.ToggleButton
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val mPlayer = MediaPlayer()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                // Home page
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                // Programming grid
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                // Settings / other stuff
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Prepare media player
        mPlayer.apply {
            setAudioAttributes(audioAttrs)
            setDataSource(streamUrl)

            // Prepare the audio stream in the background, and enable the play button only when ready
            prepareAsync()
            setOnPreparedListener {
                playButton.isEnabled = true
            }
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
    }

    fun onPlayPause(v: View) {
        val button = v as? ToggleButton ?: return
        try {
            if (!mPlayer.isPlaying) {
                mPlayer.start()
            } else {
                mPlayer.pause()
            }
        } catch (e: Exception) {
            button.isChecked = !button.isChecked
            e.printStackTrace()
        }
    }

    companion object {
        private val audioAttrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()
        private const val streamUrl = "http://archives.kdvs.org:8000/kdvs128mp3"
    }
}
