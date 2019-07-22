package fho.kdvs.global.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import fho.kdvs.R

/** Toggling a loading view to hide observable data pop-in. */
object LoadScreen {
    private const val tagStr = "Loading"

    @JvmStatic
    fun displayLoadScreen(base: ViewGroup) {
        val layout = RelativeLayout(base.context)
        val progressBar = ProgressBar(base.context)

        layout.apply {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )

            layoutParams = params
            tag = tagStr
            visibility = View.VISIBLE
            elevation = 2f

            setBackgroundColor(base.resources.getColor(R.color.colorPrimaryDark, base.context.theme))
        }

        progressBar.apply {
            isIndeterminate = true
        }

        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )

        params.addRule(RelativeLayout.CENTER_IN_PARENT)

        layout.addView(progressBar, params)
        
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