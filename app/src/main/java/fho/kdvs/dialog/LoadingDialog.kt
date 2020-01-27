package fho.kdvs.dialog

import android.app.Dialog
import android.content.Context
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import fho.kdvs.R
import fho.kdvs.global.ui.Displayable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Cancellable loading dialog with timeout.
 */
class LoadingDialog(
    private val context: Context,
    private val label: String,
    private val errorMessage: String,
    private val timeout: Long = 8000L,
    private val onCancel: (() -> Unit)?
): Displayable, CoroutineScope {
    private lateinit var dialog: Dialog

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    override fun display() {
        initializeTimeout()

        dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.dialog_loading)
        dialog.findViewById<TextView>(R.id.loading_label).text = label
        dialog.setOnCancelListener {
            onCancel?.invoke()
        }

        dialog.show()
    }

    override fun hide() {
        dialog.dismiss()
    }

    private fun initializeTimeout() {
        launch {
            delay(timeout)

            Toast.makeText(
                context,
                errorMessage,
                Toast.LENGTH_SHORT
            ).show()

            onCancel?.invoke()

            hide()
        }
    }
}