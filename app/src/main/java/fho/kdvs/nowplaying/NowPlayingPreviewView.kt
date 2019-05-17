package fho.kdvs.nowplaying

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
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
}