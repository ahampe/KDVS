package fho.kdvs.nowplaying

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.LinearLayout
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.view_now_playing.view.*

class NowPlayingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    LinearLayout(context, attrs, defStyleAttr) {

    /** Called when this view is single-clicked */
    var onClickHandler: (() -> Unit)? = null

    /** Called when this view is swiped vertically. Boolean passed indicates if swipe is up or down. */
    var onSwipeHandler: ((Boolean) -> Unit)? = null

//    init {
//        setOnTouchListener(object : NowPlayingTouchListener(context) {
//            override fun onClick() {
//                performClick()
//                onClickHandler?.invoke()
//            }
//
//            override fun onSwipeUp() {
//                onSwipeHandler?.invoke(true)
//            }
//
//            override fun onSwipeDown() {
//                onSwipeHandler?.invoke(false)
//            }
//        })
//    }

    fun setCurrentShowTitle(showTitle: String?) {
        previewShowTitle.text = showTitle ?: "..."
    }

    fun setCurrentShowImage(imageUrl: String?) {
        Glide.with(context)
            .load(imageUrl)
            .into(playing_image)
    }

    open class NowPlayingTouchListener(context: Context) : View.OnTouchListener {
        private val gestureDetector = GestureDetector(context, NowPlayingGestureListener())

        @SuppressLint("ClickableViewAccessibility") // the view IS clicked in a roundabout way
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            return gestureDetector.onTouchEvent(event)
        }

        inner class NowPlayingGestureListener : GestureDetector.SimpleOnGestureListener() {
            override fun onDown(e: MotionEvent?): Boolean {
                return true
            }

            override fun onSingleTapUp(e: MotionEvent?): Boolean {
                onClick()
                return super.onSingleTapUp(e)
            }

            override fun onFling(e1: MotionEvent?, e2: MotionEvent?, velocityX: Float, velocityY: Float): Boolean {
                if (e1 == null || e2 == null) return false

                val dy = e2.y - e1.y
                val dx = e2.x - e1.x
                if (Math.abs(dy) > Math.abs(dx)) {
                    if (Math.abs(dy) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                        if (dy > 0) {
                            onSwipeDown()
                        } else {
                            onSwipeUp()
                        }
                    }
                }
                return false
            }
        }

        open fun onClick() {}

        open fun onSwipeUp() {}

        open fun onSwipeDown() {}

        companion object {
            private const val SWIPE_THRESHOLD = 100
            private const val SWIPE_VELOCITY_THRESHOLD = 100
        }
    }


}

