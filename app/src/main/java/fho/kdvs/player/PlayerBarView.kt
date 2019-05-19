package fho.kdvs.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import fho.kdvs.R
import fho.kdvs.global.SharedViewModel
import kotlinx.android.synthetic.main.player_bar_view.view.*
import timber.log.Timber

class PlayerBarView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    lateinit var mNavController: NavController

    init {
        setOnClickListener {
            Timber.d("Clicked nowPlaying preview")
            navigateToPlayer()
        }
    }

    fun setCurrentShowName(name: String?) {
        showName.text = name ?: ""
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

    fun initButtonClickListener(vm: SharedViewModel) {
        previewPlayPauseIcon.setOnClickListener {
            vm.playOrPausePlayback()
        }
    }

    private fun navigateToPlayer() {
        mNavController.navigate(R.id.playerFragment)
    }
}