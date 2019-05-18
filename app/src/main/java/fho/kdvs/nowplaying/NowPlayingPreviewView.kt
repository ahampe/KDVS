package fho.kdvs.nowplaying

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import fho.kdvs.R
import fho.kdvs.global.SharedViewModel
import kotlinx.android.synthetic.main.view_now_playing_preview.view.*
import timber.log.Timber

class NowPlayingPreviewView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    init {
        setOnClickListener {
            Timber.d("Clicked it")
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
        playPauseIcon.setOnClickListener {
            vm.playOrPausePlaybackAndToggleImage(it)
        }
    }

    fun start() {
        playPauseIcon.setImageResource(R.drawable.ic_pause_circle_outline_white_48dp)
    }

    fun pause() {
        playPauseIcon.setImageResource(R.drawable.ic_play_circle_outline_white_48dp)
    }
}