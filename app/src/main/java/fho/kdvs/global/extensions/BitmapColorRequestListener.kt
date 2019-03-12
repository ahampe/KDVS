package fho.kdvs.global.extensions

import android.content.Context
import android.graphics.Bitmap
import android.view.View
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import fho.kdvs.R
import fho.kdvs.global.util.ColorHelper
import fho.kdvs.global.util.SeedHelper


class BitmapColorRequestListener(
    val context: Context,
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
            val isPlaceholder = seedStr.contains(context.getString(R.string.timeslot_placeholder))
            val seed = if (isPlaceholder) Math.random().toLong() else SeedHelper.getSeedFromStr(seedStr)

            val randomMatColor = ColorHelper.getRandomMatColor(500, context, seed)

            if (ColorHelper.isGrayscaleImage(resource)) {
                viewToColor.setBackgroundColor(randomMatColor)
            }
            else {
                val p = Palette.from(resource).generate()
                viewToColor.setBackgroundColor(p.getDarkMutedColor(randomMatColor))
            }
        }

        return false
    }
}