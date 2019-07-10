package fho.kdvs.global.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import fho.kdvs.R

/** Toggling a loading view to hide observable data pop-in. */
object LoadScreen {
    private const val tagStr = "Loading"

    @JvmStatic
    fun displayLoadScreen(base: ViewGroup) {
        val layout = ConstraintLayout(base.context)
        val progressBar = ProgressBar(base.context)
        val set = ConstraintSet()

        layout.apply {
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.MATCH_PARENT
            )

            layoutParams = params
            tag = tagStr
            visibility = View.VISIBLE
            elevation = 2f
            setBackgroundColor(base.resources.getColor(R.color.colorPrimaryDark, base.context.theme))
        }

        progressBar.apply {
            val params = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

            layoutParams = params
            isIndeterminate = true
        }

        set.apply {
            centerHorizontally(progressBar.id, layout.id)
            centerVertically(progressBar.id, layout.id)
            applyTo(layout)
        }

        layout.addView(progressBar)
        base.addView(layout)
    }

    @JvmStatic
    fun hideLoadScreen(base: ViewGroup) {
        for (i in 0..base.childCount) {
            if (base.getChildAt(i)?.tag == tagStr) {
                base.getChildAt(i)?.visibility = View.GONE
                return
            }
        }
    }
}