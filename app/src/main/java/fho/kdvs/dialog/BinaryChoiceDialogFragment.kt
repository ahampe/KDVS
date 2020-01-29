package fho.kdvs.dialog

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import fho.kdvs.R

/**
 * Displays a yes/no dialog.
 */
class BinaryChoiceDialogFragment : DialogFragment() {
    private val title: String by lazy {
        arguments?.let { BinaryChoiceDialogFragmentArgs.fromBundle(it) }?.title
            ?: throw IllegalArgumentException("Should have passed a title to BinaryChoiceDialogFragment")
    }

    private val message: String by lazy {
        arguments?.let { BinaryChoiceDialogFragmentArgs.fromBundle(it) }?.message
            ?: throw IllegalArgumentException("Should have passed a message to BinaryChoiceDialogFragment")
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setIcon(R.drawable.ic_announcement_white_24dp)
            .setPositiveButton(android.R.string.yes,
                { _, _ ->
                    targetFragment
                        ?.onActivityResult(targetRequestCode, Activity.RESULT_OK, null)
                })
            .setNegativeButton(android.R.string.no,
                DialogInterface.OnClickListener { _, _ ->
                    targetFragment
                        ?.onActivityResult(targetRequestCode, Activity.RESULT_CANCELED, null)
                }).create()
    }
}