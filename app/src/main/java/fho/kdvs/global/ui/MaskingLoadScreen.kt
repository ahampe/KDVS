package fho.kdvs.global.ui

import android.view.ViewGroup
import android.widget.RelativeLayout
import fho.kdvs.R
import java.lang.ref.WeakReference

/**
 * Opaque loading screen to mask pop-in.
 * */
class MaskingLoadScreen(rootRef: WeakReference<ViewGroup>) :
    Displayable {
    val root = rootRef.get()

    private val layout = RelativeLayout(root?.context)
    private val loadScreen = LoadScreen(root, true)

    override fun display() {
        root?.let {
            layout.apply {
                setBackgroundColor(
                    root.resources.getColor(
                        R.color.colorPrimaryDark,
                        root.context.theme
                    )
                )
            }
        }

        loadScreen.display()
    }

    override fun hide() {
        loadScreen.hide()
    }
}