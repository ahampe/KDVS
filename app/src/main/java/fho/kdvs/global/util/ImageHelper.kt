package fho.kdvs.global.util

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import fho.kdvs.R
import fho.kdvs.global.extensions.ReflectionRequestListener

object ImageHelper {
    fun loadImageWithGlide(view: ImageView, imageHref: String?) {
        Glide.with(view)
            .asBitmap()
            .load(imageHref)
            .transition(BitmapTransitionOptions.withCrossFade())
            .apply(
                RequestOptions()
                    .apply(RequestOptions.centerCropTransform())
                    .error(R.drawable.show_placeholder)
            )
            .into(view)
    }

    fun loadImageAndReflectionWithGlide(view: ImageView, imageHref: String?) {
        Glide.with(view)
            .asBitmap()
            .load(imageHref)
            .transition(BitmapTransitionOptions.withCrossFade())
            .apply(
                RequestOptions()
                    .apply(RequestOptions.centerCropTransform())
                    .error(R.drawable.show_placeholder)
            )
            .transition(BitmapTransitionOptions.withCrossFade())
            .listener(
                ReflectionRequestListener(view)
            )
            .into(view)
    }
}