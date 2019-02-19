package fho.kdvs.broadcast

import android.widget.LinearLayout
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import fho.kdvs.R

@BindingAdapter("trackPosition")
fun alternateTrackBackground(layout: LinearLayout, position: Int?) {
    val i = position ?: return

    val color = when {
        i % 2 == 0 -> ResourcesCompat.getColor(layout.resources, R.color.colorBlack50a, layout.context.theme)
        else -> ResourcesCompat.getColor(layout.resources, R.color.colorPrimaryDark50a, layout.context.theme)
    }

    layout.setBackgroundColor(color)
}