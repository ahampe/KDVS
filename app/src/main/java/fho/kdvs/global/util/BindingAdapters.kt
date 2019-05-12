package fho.kdvs.global.util

import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import fho.kdvs.R
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter

// Binding adapters designed for reuse

@BindingAdapter("glideHref")
fun loadImageWithGlide(view: ImageView, imageHref: String?) {
    ImageHelper.loadImageWithGlide(view, imageHref)
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

@BindingAdapter("trackInfo")
fun formatTrackInfo(view: TextView, track: TrackEntity?) {
    if (!track?.artist.isNullOrBlank() && !track?.song.isNullOrBlank()){
        var trackInfo = track?.artist
        
        if (!track?.album.isNullOrBlank())
            trackInfo += view.resources.getString(R.string.track_info_middle, track?.album)

        view.text = trackInfo
    }
}

@BindingAdapter("artist", "album")
fun formatArtistAlbum(view: TextView, artist: String?, album: String?) {
    val albumStr = view.resources.getString(R.string.track_info_middle, album)

    if (albumStr.isNullOrBlank())
        view.text = artist
    else
        view.text = view.resources.getString(R.string.artist_album, artist, album)
}

@BindingAdapter("year", "label")
fun formatAlbumInfo(view: TextView, year: Int?, label: String?) {
    if (year != null || label != null) {
        if (label == null) {
            view.text = year.toString()
        } else if (year == null) {
            view.text = label
        } else {
            view.text = view.resources.getString(R.string.album_info, year, label)
        }
    } else view.visibility = View.GONE
}

@BindingAdapter("comment")
fun formatComment(view: TextView, comment: String?) {
    if (!comment.isNullOrBlank()) {
        view.text = view.resources.getString(R.string.track_comments, comment)
        view.visibility = View.VISIBLE
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

