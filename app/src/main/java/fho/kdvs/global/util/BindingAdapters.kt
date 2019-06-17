package fho.kdvs.global.util

import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.BindingAdapter
import fho.kdvs.R
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TopMusicEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.web.SpotifyData
import fho.kdvs.global.web.uri
import fho.kdvs.topmusic.TopMusicType
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

// Binding adapters designed for reuse

@BindingAdapter("glideHref")
fun loadImageWithGlide(view: ImageView, imageHref: String?) {
    ImageHelper.loadImageWithGlide(view, imageHref)
}

@BindingAdapter("glideHrefGradient")
fun loadTopMusicImageWithGlideAndApplyGradient(view: ImageView, imageHref: String?) {
    val parent = view.parent.parent.parent as ConstraintLayout
    imageHref?.let {
        ImageHelper.loadImageWithGlideAndApplyGradient(view, parent, imageHref)
    }
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

@BindingAdapter("comment")
fun displayComment(view: ImageView, comment: String?) {
    if (!comment.isNullOrBlank())
        view.visibility = View.VISIBLE
}

@BindingAdapter("favorite")
fun displayFavorite(view: ImageView, favorite: Boolean) {
    if (favorite) {
        view.tag = 1
        view.setImageResource(R.drawable.ic_favorite_white_24dp)
    } else {
        view.tag = 0
        view.setImageResource(R.drawable.ic_favorite_border_white_24dp)
    }
}

@BindingAdapter("topMusicAlbumInfo")
fun formatTopMusicAlbumInfo(view: TextView, topMusic: TopMusicEntity?) {
    topMusic?.let {
        if (it.label != null || it.year != null) {
            if (it.label == null) {
                view.text = it.year?.toString()
            } else if (it.year == null) {
                view.text = it.label
            } else {
                view.text = view.resources.getString(
                    R.string.album_info,
                    it.year, it.label
                )
            }
        }
    }
}

@BindingAdapter("topMusicHeader")
fun formatTopMusicHeader(view: TextView, type: TopMusicType) {
    view.text = when (type) {
        TopMusicType.ADD -> "Top adds"
        TopMusicType.ALBUM -> "Top albums"
    }
}

@BindingAdapter("position")
fun formatTopMusicPosition(view: TextView, position: Int?) {
    position?.let {
        view.text = (it + 1).toString()
    }
}

@BindingAdapter("weekOf")
fun formatTopMusicWeekOf(view: TextView, weekOf: LocalDate?) {
    weekOf?.let {
        val formatter = TimeHelper.uiDateFormatter
        view.text = formatter.format(weekOf)
    }
}

@BindingAdapter("spotifyData")
fun displaySpotifyIcon(view: ImageView, spotifyData: SpotifyData?) {
    view.visibility = if (spotifyData != null && !spotifyData.uri.isNullOrBlank())
        View.VISIBLE
    else View.GONE
}

@BindingAdapter("trackInfo")
fun formatTrackInfo(view: TextView, track: TrackEntity?) {
    if (!track?.artist.isNullOrBlank() && !track?.song.isNullOrBlank()){
        var trackInfo = track?.artist
        
        if (!track?.album.isNullOrBlank())
            trackInfo += view.resources.getString(R.string.track_info_middle, track?.album)

        view.text = trackInfo
    }
}

//@BindingAdapter("trackPosition")
//fun alternateTrackBackground(layout: ConstraintLayout, position: Int?) {
//    val i = position ?: return
//
//    val color = when {
//        i % 2 == 0 -> ResourcesCompat.getColor(layout.resources, R.color.colorBlack50a, layout.context.theme)
//        else -> ResourcesCompat.getColor(layout.resources, R.color.colorPrimaryDark50a, layout.context.theme)
//    }
//
//    layout.setBackgroundColor(color)
//}

@BindingAdapter("nextShow", "isStreamingLive")
fun updateNextLiveShow(view: TextView, nextShow: ShowEntity, isStreamingLive: Boolean) {
    // don't update anything if we're not streaming live right now
    if (!isStreamingLive) return

    // TODO
}

