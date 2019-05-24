package fho.kdvs.global.ui

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.palette.graphics.Palette
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.narayanacharya.waveview.WaveView
import fho.kdvs.R
import fho.kdvs.global.util.ColorHelper
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.schedule.TimeSlot
import timber.log.Timber

interface IPaletteRequestListener  {
    fun getColorFromPalette(bitmap: Bitmap)
    fun setTargetView()
}

/** Class for applying dynamic [Palette] coloration and gradient to Player. */
class PlayerPaletteRequestListener (
    private val viewToColor: View
) : RequestListener<Bitmap>, IPaletteRequestListener {

    private var selectedColorLight: Int = viewToColor.resources.getColor(
        R.color.colorPrimary,
        viewToColor.context.theme)
    private var selectedColorDark: Int = viewToColor.resources.getColor(
        R.color.colorPrimaryDark,
        viewToColor.context.theme)

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Bitmap>,
        isFirstResource: Boolean
    ) : Boolean {
        setTargetView()
        return false
    }

    override fun onResourceReady(
        resource: Bitmap?,
        model: Any,
        target: Target<Bitmap>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        if (resource == null) return false

        getColorFromPalette(resource)
        setTargetView()

        return false
    }

    override fun getColorFromPalette(bitmap: Bitmap) {
        selectedColorLight = Palette.from(bitmap).generate().getLightMutedColor(selectedColorLight)
        selectedColorDark = Palette.from(bitmap).generate().getDarkMutedColor(selectedColorDark)
    }

    override fun setTargetView() {
        setViewGradient()
    }

    /** Set color to black Top->Down gradient */
    private fun setViewGradient() {
        val backgroundColors = intArrayOf(selectedColorLight, selectedColorDark)
        viewToColor.background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, backgroundColors)
    }
}

/**
 * Class for applying dynamic [Palette] coloration and alpha gradients to [TimeSlot]s. Placeholder [TimeSlot]s are 
 * assigned random color deterministically by their corresponding show names. [WaveView] animation applied to 
 * current show [TimeSlot] on schedule.
 * */

class TimeSlotPaletteRequestListener (
    private val viewWithColor: View,
    private val viewToColor: View,
    private val timeslot: TimeSlot?
) : RequestListener<Bitmap>, IPaletteRequestListener {
    
    private var isPlaceholder = false
    private var selectedColor = 0
    private var seed = Long.MIN_VALUE

    override fun onLoadFailed(
        e: GlideException?,
        model: Any,
        target: Target<Bitmap>,
        isFirstResource: Boolean
    ) : Boolean {

        isPlaceholder = true

        setSeed()
        setRandomColor()
        setTargetView()

        return false
    }

    override fun onResourceReady(
        resource: Bitmap?,
        model: Any,
        target: Target<Bitmap>,
        dataSource: DataSource,
        isFirstResource: Boolean
    ): Boolean {
        if (resource == null || timeslot == null) return false

        setSeed()
        setRandomColor()
        if (!isPlaceholder()) getColorFromPalette(resource)
        setTargetView()

        return false
    }

    private fun isPlaceholder(): Boolean{
        return ((timeslot?.imageHref ?: "").contains(".*kdvs.org.*placeholder.*".toRegex())) // TODO: don't rely on static kdvs placeholder?
        // || resource == (viewWithColor.context.getDrawable(R.drawable.show_placeholder) as BitmapDrawable).bitmap // this produces glitches when shows aren't loaded in yet
    }

    /** Use showName to generate random color */
    private fun setSeed() {
        seed = (timeslot?.names?.first() ?: "").hashCode().toLong()
    }

    override fun getColorFromPalette(bitmap: Bitmap) {
        selectedColor = Palette.from(bitmap).generate().getDarkVibrantColor(selectedColor)
    }

    private fun setRandomColor() {
        // TODO: make random color fit with theme
        // TODO: make colortype dynamic?
        selectedColor = ColorHelper.getRandomMatColor(500, viewWithColor.context, seed)
    }

    override fun setTargetView() {
        setViewColor()
        setViewTransparency()
        setViewSineWave()
    }

    private fun setViewColor() {
        viewToColor.setBackgroundColor(selectedColor)
    }

    /** Set transparent to opaque Left->Right gradient */
    private fun setViewTransparency() {
        val backgroundColors = intArrayOf(selectedColor, Color.TRANSPARENT)
        viewWithColor.foreground = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, backgroundColors)
    }

    /** Sine wave animation overlay on current timeslot */
    private fun setViewSineWave() {
        if (timeslot == null) return

        val waveView = viewToColor.findViewById<WaveView>(R.id.waveView)
        if (TimeHelper.isTimeSlotForCurrentShow(timeslot)) {
            Timber.d("currentShow $timeslot.names.firstOrNull()")
            if (waveView != null && waveView.visibility != View.VISIBLE){
                waveView.backgroundColor = selectedColor
                waveView.waveColor = ColorHelper.getComplementaryColor(selectedColor, viewWithColor.context)
                waveView.visibility = View.VISIBLE
            }
        } else {
            waveView.visibility = View.GONE
        }
    }
}