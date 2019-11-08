package fho.kdvs.favorite

import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import fho.kdvs.R
import java.lang.ref.WeakReference

interface FavoritePage<T> {
    fun subscribeToViewModel()
    fun processFavorites(joins: List<T>?)
    fun setSectionHeaders()
    fun clearSectionHeaders()
    fun initializeClickListeners()
    fun initializeSearchBar()
}

class FavoritePageHelper<J>(
    val favoriteViewAdapter: FavoriteViewAdapter?,
    val sortMenu: WeakReference<View>,
    val dummy: WeakReference<View>,
    val filter: WeakReference<View>
) {
    val hashedResults = mutableMapOf<String, ArrayList<J>>()
    val currentlyDisplayingResults = mutableListOf<J?>()

    var sortType = FavoriteFragment.SortType.RECENT
    var sortDirection = FavoriteFragment.SortDirection.DES

    fun initializeClickListeners() {
        val sortRecent = sortMenu.get()?.findViewById<ConstraintLayout>(R.id.sortRecent)
        val sortShow = sortMenu.get()?.findViewById<ConstraintLayout>(R.id.sortShow)
        val sortArtist = sortMenu.get()?.findViewById<ConstraintLayout>(R.id.sortArtist)
        val sortAlbum = sortMenu.get()?.findViewById<ConstraintLayout>(R.id.sortAlbum)
        val sortTrack = sortMenu.get()?.findViewById<ConstraintLayout>(R.id.sortTrack)

        val layoutToSortType = listOf(
            Pair(sortRecent, FavoriteFragment.SortType.RECENT),
            Pair(sortShow, FavoriteFragment.SortType.SHOW),
            Pair(sortArtist, FavoriteFragment.SortType.ARTIST),
            Pair(sortAlbum, FavoriteFragment.SortType.ALBUM),
            Pair(sortTrack, FavoriteFragment.SortType.TRACK)
        )

        dummy.get()?.setOnClickListener {
            sortMenu.get()?.visibility = View.GONE
            dummy.get()?.visibility = View.GONE
        }

        filter.get()?.setOnClickListener {
            sortMenu.get()?.visibility = if (sortMenu.get()?.visibility == View.GONE)
                View.VISIBLE else View.GONE
            dummy.get()?.visibility = if (sortMenu.get()?.visibility == View.GONE)
                View.VISIBLE else View.GONE
        }

        layoutToSortType.forEach { pair ->
            val layout = pair.first
            val button = layout?.getChildAt(1) as? ImageView

            layout?.visibility = View.VISIBLE

            layout?.setOnClickListener {
                button?.visibility = View.VISIBLE

                sortType = pair.second

                if (button?.tag == FavoriteFragment.SortDirection.ASC.type) {
                    button.tag = FavoriteFragment.SortDirection.DES.type
                    button.setImageResource(R.drawable.ic_arrow_upward_white_24dp)
                    sortDirection = FavoriteFragment.SortDirection.DES

                } else if (button?.tag == FavoriteFragment.SortDirection.DES.type) {
                    button.tag = FavoriteFragment.SortDirection.ASC.type
                    button.setImageResource(R.drawable.ic_arrow_downward_white_24dp)
                    sortDirection = FavoriteFragment.SortDirection.ASC
                }

                favoriteViewAdapter?.updateData()

                val otherPairs = layoutToSortType.filter { p -> p != pair }
                otherPairs.forEach { p ->
                    val otherButton = p.first?.getChildAt(1)
                    otherButton?.visibility = View.INVISIBLE
                }
            }
        }
    }
}