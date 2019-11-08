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
    fun displayLoadScreen(root: ViewGroup?, offsetFromBottom: Boolean = true) {
        if (root == null) return

        val layout = RelativeLayout(root.context)
        val progressBar = ProgressBar(root.context)

        layout.apply {
            val params = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT
            )

            if (offsetFromBottom)
                params.bottomMargin = root.resources.getDimension(R.dimen.bottom_nav_height).toInt()

            layoutParams = params
            tag = tagStr
            visibility = View.VISIBLE
            elevation = 2f

            setBackgroundColor(
                root.resources.getColor(
                    R.color.colorPrimaryDark,
                    root.context.theme
                )
            )
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

        root.addView(layout)
    }

    @JvmStatic
    fun hideLoadScreen(root: ViewGroup?) {
        if (root == null) return

        for (i in 0..root.childCount) {
            if (root.getChildAt(i)?.tag == tagStr) {
                root.getChildAt(i)?.visibility = View.GONE
                return
            }
        }
    }
}