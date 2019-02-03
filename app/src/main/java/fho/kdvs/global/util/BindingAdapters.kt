package fho.kdvs.global.util

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import fho.kdvs.R

// Binding adapters designed for reuse

@BindingAdapter("glideHref")
fun loadImageWithGlide(view: ImageView, imageHref: String?) {
    Glide.with(view)
        .applyDefaultRequestOptions(
            RequestOptions()
                .apply(RequestOptions.centerCropTransform())
                .error(R.drawable.show_placeholder)
        )
        .load(imageHref)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(view)
}