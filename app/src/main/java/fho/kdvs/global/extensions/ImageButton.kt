package fho.kdvs.global.extensions

import android.content.Context
import android.widget.ImageButton
import fho.kdvs.global.util.ColorHelper.convertDrawableToGrayScale

/**
 * Toggles isEnabled and grayscale filter.
 */
fun ImageButton.setButtonEnabled(context: Context?, enabled: Boolean, iconResId: Int) {
    isEnabled = enabled
    val originalIcon = context?.resources?.getDrawable(iconResId, context.theme)
    val icon = if (enabled) originalIcon else convertDrawableToGrayScale(originalIcon)
    setImageDrawable(icon)
}