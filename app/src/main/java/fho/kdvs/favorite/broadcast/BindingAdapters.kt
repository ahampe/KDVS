package fho.kdvs.favorite.broadcast

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import fho.kdvs.R
import fho.kdvs.global.SharedViewModel
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.ShowEntity

@BindingAdapter("sharedVm", "broadcast", "show")
fun initDownloadObserver (
    view: ImageView,
    sharedVm: SharedViewModel,
    broadcast: BroadcastEntity,
    show: ShowEntity
) {
    val title = sharedVm.getBroadcastDownloadTitle(broadcast, show)
    val onComplete = {
        view.setImageDrawable(
            view.resources.getDrawable(
                R.drawable.ic_delete_forever_white_24dp,
                view.context.theme
            )
        )
    }
    val onDelete = {
        view.setImageDrawable(
            view.resources.getDrawable(
                R.drawable.ic_file_download_white_24dp,
                view.context.theme
            )
        )
    }

    sharedVm.callOnFileEventForFilename(title, onComplete, onDelete)
}