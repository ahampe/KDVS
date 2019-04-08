package fho.kdvs.home

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import fho.kdvs.R

@BindingAdapter("glideHrefDefaultGone")
fun loadImageWithGlideIfPresent(view: ImageView, imageHref: String?) {
    if (imageHref != null){
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
}

@BindingAdapter("position", "artist", "album")
fun bindTopMusicCell(view: TextView, position: Int, artist: String, album: String) {
    view.text = position.toString() + ". " + artist + " - " + album
}