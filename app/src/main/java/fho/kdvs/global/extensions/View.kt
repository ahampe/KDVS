package fho.kdvs.global.extensions

import android.view.View
import android.view.ViewGroup
import androidx.transition.Fade
import androidx.transition.TransitionManager


/**
 * Fades a view in or out with transition animation. Use to prevent abrupt pop-in.
 */
fun View?.fade(visible: Boolean) {
    this?.let {
        val transition = Fade()
        transition.duration = 300
        transition.addTarget(it)

        TransitionManager.beginDelayedTransition(it.parent as ViewGroup, transition)
        it.visibility = if (visible) View.VISIBLE else View.GONE
    }
}