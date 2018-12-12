package fho.kdvs

import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var mPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        playButton.setOnClickListener { playButton ->
            // Disable the play button
            playButton.isEnabled = false
            pauseButton.isEnabled = true

            // The audio url to play
            val audioUrl = "http://archives.kdvs.org:8000/kdvs128mp3"

            // Initialize a new media player instance
            mPlayer = MediaPlayer()

            // Set audio attributes
            val audioAttrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            // Set the media player audio stream type
            mPlayer?.setAudioAttributes(audioAttrs)
            //Try to play music/audio from url
            try {
                // Set the audio data source
                mPlayer?.setDataSource(audioUrl)

                // Prepare the media player
                mPlayer?.prepare()

                // Start playing audio from http url
                mPlayer?.start()

                // Inform user for audio streaming
                Toast.makeText(this, "Playing", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                // Catch the exception
                e.printStackTrace()
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            } catch (e: SecurityException) {
                e.printStackTrace()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            }
        }

        pauseButton.setOnClickListener { _ ->
            mPlayer?.pause()
            playButton.isEnabled = true
            pauseButton.isEnabled = false
        }
    }
}

//class MainActivity : AppCompatActivity() {
//
//    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
//        when (item.itemId) {
//            R.id.navigation_home -> {
//                message.setText(R.string.title_home)
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_dashboard -> {
//                message.setText(R.string.title_dashboard)
//                return@OnNavigationItemSelectedListener true
//            }
//            R.id.navigation_notifications -> {
//                message.setText(R.string.title_notifications)
//                return@OnNavigationItemSelectedListener true
//            }
//        }
//        false
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//
//        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)
//    }
//}
