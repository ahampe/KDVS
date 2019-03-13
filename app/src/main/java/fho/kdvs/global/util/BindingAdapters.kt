package fho.kdvs.global.util

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import fho.kdvs.R
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