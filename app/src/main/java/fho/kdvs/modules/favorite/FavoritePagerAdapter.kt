package fho.kdvs.modules.favorite

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import fho.kdvs.favorite.broadcast.FavoriteBroadcastFragment
import fho.kdvs.favorite.track.FavoriteTrackFragment

class FavoritePagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    val favoriteTrackFrag = FavoriteTrackFragment()
    val favoriteBroadcastFrag = FavoriteBroadcastFragment()

    override fun getCount() = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            POS_TRACKS -> favoriteTrackFrag
            POS_BROADCASTS -> favoriteBroadcastFrag
            else -> FavoriteTrackFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            POS_TRACKS -> "Tracks"
            POS_BROADCASTS -> "Broadcasts"
            else -> null
        }
    }
}
