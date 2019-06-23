package fho.kdvs.home

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import fho.kdvs.R
import fho.kdvs.global.ui.CurrentShowPaletteRequestListener
import fho.kdvs.global.util.ImageHelper
import kotlinx.android.synthetic.main.fragment_home.view.*

@BindingAdapter("currentShowGlideHrefGradient")
fun loadCurrentShowImageWithGlideAndApplyGradient(view: ImageView, imageHref: String?) {
    when ((view.parent as? View)?.tag != null) {
        true -> { // on now
            val parent = view.parent.parent.parent.parent as ConstraintLayout
            val target = parent.defaultGradient
            val listener = CurrentShowPaletteRequestListener(target)
            imageHref?.let {
                ImageHelper.loadImageWithGlideAndApplyGradient(view, listener, imageHref)
            }
        }
        false -> {
            imageHref?.let {
                ImageHelper.loadImageWithGlide(view, imageHref)
            }
        }
    }
}


@BindingAdapter("glideHrefDefaultGone")
fun loadImageWithGlideIfPresent(view: ImageView, imageHref: String?) {
    if (!imageHref.isNullOrBlank()){
        Glide.with(view)
            .applyDefaultRequestOptions(
                RequestOptions()
                    .apply(RequestOptions.centerCropTransform())
                    .error(R.drawable.show_placeholder)
            )
            .load(imageHref)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)

        view.visibility = View.VISIBLE
    }
}