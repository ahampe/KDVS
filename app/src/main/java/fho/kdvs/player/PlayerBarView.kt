package fho.kdvs.player

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.navigation.NavController
import fho.kdvs.R
import fho.kdvs.global.SharedViewModel
import kotlinx.android.synthetic.main.player_bar_view.view.*
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import java.lang.ref.WeakReference

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

    fun initButtonClickListener(vm: SharedViewModel) {
        previewPlayPauseIcon.setOnClickListener {
            vm.playOrPausePlayback()
        }
    }

    fun initProgressBar(timeStart: OffsetDateTime, timeEnd: OffsetDateTime) {
        val weakPB = WeakReference<ProgressBar>(barProgressBar)
        val progressAsyncTask = TimeProgressAsyncTask(weakPB, timeStart, timeEnd)
        progressAsyncTask.execute()
    }

    private fun navigateToPlayer() {
        mNavController.navigate(R.id.playerFragment)
    }
}