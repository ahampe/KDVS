package fho.kdvs.nowplaying

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_now_playing.view.*
import timber.log.Timber

class NowPlayingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    init {
        setOnClickListener {
            Timber.d("Clicked it")
        }
    }

    fun setCurrentShowName(showTitle: String?) {
        previewShowTitle.text = showTitle ?: "..."
    }

    fun setCurrentShowImage(imageUrl: String?) {
        Glide.with(context)
            .load(imageUrl)
            .into(playing_image)
    }

    fun setArchiveShowDate() {

    }

    fun setLiveShowTime(showTime: String?) {
        //showTime.text = showTime ?: ""
    }

    fun setArchiveShow() {
        //liveIcon.visibility = View.GONE
    }

    fun setLiveShow() {
        //liveIcon.visibility = View.VISIBLE
    }
}
