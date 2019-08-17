package fho.kdvs.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.google.android.exoplayer2.ExoPlayer
import fho.kdvs.global.SharedViewModel
import kotlinx.android.synthetic.main.view_player_bar.view.*
import timber.log.Timber

class PlayerBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    lateinit var mNavController: NavController
    lateinit var sharedViewModel: SharedViewModel
    lateinit var mExoPlayer: ExoPlayer
    lateinit var mActivity: FragmentActivity

    init {
        setOnClickListener {
            Timber.d("Clicked nowPlaying preview")
            navigateToPlayer()
        }
    }

    fun setCurrentShowName(name: String?) {
        barShowName.text = name ?: ""
    }

    fun setShowTimeOrBroadcastDate(timeOrDate: String?) {
        showTimeOrBroadcastDate.text = timeOrDate ?: ""
    }

    fun initArchiveShow() {
        liveIcon.visibility = View.GONE
    }

    fun initLiveShow() {
        liveIcon.visibility = View.VISIBLE
    }

    fun initButtonClickListener() {
        playerBayPlayPause.setOnClickListener {
            if (::sharedViewModel.isInitialized)
                sharedViewModel.playOrPausePlayback(mActivity)
        }
    }

    private fun navigateToPlayer() {
        if (::sharedViewModel.isInitialized)
            sharedViewModel.navigateToPlayer(mNavController)
    }
}