package fho.kdvs.global.util

import android.text.Html
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ui.PlayerView
import fho.kdvs.R
import fho.kdvs.global.database.ShowEntity
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

// Binding adapters designed for reuse

@BindingAdapter("glideHref")
fun loadImageWithGlide(view: ImageView, imageHref: String?) {
    Glide.with(view)
        .asBitmap()
        .load(imageHref)
        .transition(BitmapTransitionOptions.withCrossFade())
        .apply(RequestOptions()
            .apply(RequestOptions.centerCropTransform())
            .error(R.drawable.show_placeholder)
        )
        .into(view)
}

@BindingAdapter("localDate", "dateFormatter")
fun safeFormatDate(view: TextView, date: LocalDate?, dateFormatter: DateTimeFormatter) {
    view.text = date?.let { dateFormatter.format(it) } ?: ""
}

@BindingAdapter("desc")
fun formatDescHtml(view: TextView, desc: String?){
    val descWithBreaks = desc?.replace("\n", "<br/><br/>")
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        view.text = Html.fromHtml(descWithBreaks ?: "", Html.FROM_HTML_MODE_LEGACY)
    } else {
        view.text = (Html.fromHtml(descWithBreaks ?: ""))
    }
}

@BindingAdapter("trackPosition")
fun alternateTrackBackground(layout: LinearLayout, position: Int?) {
    val i = position ?: return

    val color = when {
        i % 2 == 0 -> ResourcesCompat.getColor(layout.resources, R.color.colorBlack50a, layout.context.theme)
        else -> ResourcesCompat.getColor(layout.resources, R.color.colorPrimaryDark50a, layout.context.theme)
    }

    layout.setBackgroundColor(color)
}

@BindingAdapter("nextShow", "isStreamingLive")
fun updateNextLiveShow(view: TextView, nextShow: ShowEntity, isStreamingLive: Boolean) {
    // don't update anything if we're not streaming live right now
    if (!isStreamingLive) return

    // TODO
}

