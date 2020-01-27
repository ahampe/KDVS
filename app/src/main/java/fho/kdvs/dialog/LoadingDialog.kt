package fho.kdvs.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import fho.kdvs.R
import fho.kdvs.global.ui.Displayable

/**
 * Cancellable loading dialog.
 */
class LoadingDialog(
    private val context: Context,
    private val onCancel: (() -> Unit)?
): Displayable {
    private lateinit var dialog: Dialog

    override fun display() {
        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_loading)

        dialog.setOnCancelListener {
            onCancel?.invoke()
        }
    }

    override fun hide() {
        dialog.dismiss()
    }
}