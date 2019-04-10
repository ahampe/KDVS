package fho.kdvs.home

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import fho.kdvs.R
import fho.kdvs.global.util.TimeHelper
import org.threeten.bp.LocalDate
import java.text.DecimalFormat

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
    view.text = view.context.resources.getString(
        R.string.top_music_item,
        position,
        artist,
        album
    )
}

@BindingAdapter("dateStart", "dateEnd")
fun bindFundraiserDates(view: TextView, dateStart: LocalDate?, dateEnd: LocalDate?) {
    val startMonthStr = TimeHelper.monthIntToStr(dateStart?.monthValue)
    val endMonthStr = TimeHelper.monthIntToStr(dateEnd?.monthValue)
    val dayStart = dateStart?.dayOfMonth.toString()
    val dayEnd = dateEnd?.dayOfMonth.toString()
    val year = dateStart?.year

    if (startMonthStr == endMonthStr)
        view.text = view.context.resources.getString(
            R.string.fundraiser_dates_same_month,
            startMonthStr,
            dayStart,
            dayEnd,
            year
        )
    else
        view.text = view.context.resources.getString(
            R.string.fundraiser_dates_diff_month,
            startMonthStr,
            dayStart,
            endMonthStr,
            dayEnd,
            year
        )
}

@BindingAdapter("goal", "current")
fun bindFundraiserTotals(view: TextView, goal: Int?, current: Int?) {
    val goalStr = DecimalFormat(",###").format(goal ?: 0)
    var currentStr = DecimalFormat(",###").format(current ?: 0)

    view.text = view.context.resources.getString(
        R.string.fundraiser_totals,
        goalStr,
        currentStr
    )
}