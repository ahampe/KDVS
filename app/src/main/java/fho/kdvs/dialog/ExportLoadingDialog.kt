package fho.kdvs.dialog

import android.content.Context
import fho.kdvs.global.ui.Displayable

/**
 * [LoadingDialog] configuration for exporting music.
 */
class ExportLoadingDialog(val context: Context, onCancel: (() -> Unit)?) : Displayable {
    private val loadingDialog = LoadingDialog(
        context,
        EXPORT_LABEL,
        EXPORT_TIMEOUT,
        EXPORT_TIMEOUT_MESSAGE,
        onCancel
    )

    override fun display() {
        loadingDialog.display()
    }

    override fun hide() {
        loadingDialog.hide()
    }

    companion object {
        const val EXPORT_LABEL = "Exporting..."
        const val EXPORT_TIMEOUT = 10000L
        const val EXPORT_TIMEOUT_MESSAGE = "Error exporting music; try again."
    }
}