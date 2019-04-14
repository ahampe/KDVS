package fho.kdvs.home

import android.view.View
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

        view.visibility = View.VISIBLE
    }
}

@BindingAdapter("position")
fun bindTopMusicPositionCell(view: TextView, position: Int) {
    val adjustedPosition = if ((position % 10) == position)
        "0$position"
    else position.toString()

    view.text = view.context.resources.getString(
        R.string.top_music_position,
        adjustedPosition
    )
}