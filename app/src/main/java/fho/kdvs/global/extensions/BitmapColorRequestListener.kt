package fho.kdvs.global.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import fho.kdvs.R
import fho.kdvs.global.util.ColorHelper
import fho.kdvs.global.util.SeedHelper


class BitmapColorRequestListener(
    val view: View,
    val viewToColor: View,
    val seedStr: String
) : RequestListener<Bitmap> {
    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Bitmap>,
        isFirstResource: Boolean
    ) : Boolean {
        return false
    }

    override fun onResourceReady(
        resource: Bitmap?,
        model: Any,
        target: Target<Bitmap>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        if (resource != null) { // TODO: cleanup, remove redundancy
            // set placeholder timeslots to independent random colors
            // TODO: determine programmatically?
            val isPlaceholder = seedStr.contains(view.context.getString(R.string.timeslot_placeholder))
            val seed = if (isPlaceholder) Math.random().toLong() else SeedHelper.getSeedFromStr(seedStr)
            var colorVal = ColorHelper.getRandomMatColor(500, view.context, seed)

            if (ColorHelper.isGrayscaleImage(resource)) {
                viewToColor.setBackgroundColor(colorVal)
            } else {
                val p = Palette.from(resource).generate()
                colorVal = p.getMutedColor(colorVal)
                viewToColor.setBackgroundColor(p.getMutedColor(colorVal))
            }

            val backgroundColors = intArrayOf(colorVal, 0xaa000000.toChar().toInt())
            val gradientDrawable = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, backgroundColors)
            view.foreground = gradientDrawable
        }

        return false
    }
}