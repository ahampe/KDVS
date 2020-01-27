package fho.kdvs.global.ui

import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.RelativeLayout
import fho.kdvs.R

const val LOAD_SCREEN_TAG = "Loading"

class LoadScreen(
    val root: ViewGroup?,
    private val offsetFromBottom: Boolean = true
) : Displayable {

    override fun display() {
        root?.let{
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
                tag = LOAD_SCREEN_TAG
                visibility = View.VISIBLE
                elevation = 2f
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
    }

    override fun hide() {
        root?.let {
            for (i in 0..root.childCount) {
                if (root.getChildAt(i)?.tag == LOAD_SCREEN_TAG) {
                    root.getChildAt(i)?.visibility = View.GONE
                    return
                }
            }
        }
    }
}