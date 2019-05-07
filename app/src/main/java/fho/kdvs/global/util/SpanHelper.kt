package fho.kdvs.global.util

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import fho.kdvs.R

object SpanHelper {
    @JvmStatic
    fun highlightSpan(v: TextView, q: String) {
        val startIndex = v.text.indexOf(q, 0, true)
        val stopIndex = startIndex + q.length

        if (startIndex != -1) {
            val spannable = SpannableString(v.text)
            val color = v.resources.getColor(R.color.colorAccent, v.context.theme)
            val spansToRemove = spannable.getSpans(0, spannable.length, ForegroundColorSpan::class.java)

            spansToRemove.forEach { spannable.removeSpan(it) }

            spannable.setSpan(
                ForegroundColorSpan(color),
                startIndex, stopIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            v.text = spannable
        }
    }
}