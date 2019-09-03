package fho.kdvs.global.extensions

import android.animation.ObjectAnimator
import android.text.TextUtils
import android.widget.TextView


fun TextView?.collapseExpand(maxLines: Int) {
    this?.let {
        if (it.maxLines == maxLines) {
            it.ellipsize = null
            it.maxLines = Integer.MAX_VALUE
        } else {
            it.ellipsize = TextUtils.TruncateAt.MARQUEE
            it.maxLines = maxLines
        }

        val animation = ObjectAnimator.ofInt(it, "maxLines", it.maxLines)
        animation.setDuration(400).start()
    }
}