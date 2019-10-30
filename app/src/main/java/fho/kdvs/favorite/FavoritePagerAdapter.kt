package fho.kdvs.favorite

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import fho.kdvs.favorite.broadcast.FavoriteBroadcastFragment
import fho.kdvs.favorite.track.FavoriteTrackFragment

class FavoritePagerAdapter(fm: FragmentManager) :
    FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getCount() = 2

    override fun getItem(position: Int): Fragment {
        return when (position) {
            1 -> FavoriteTrackFragment()
            2 -> FavoriteBroadcastFragment()
            else -> FavoriteTrackFragment()
        }
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return when (position) {
            1 -> "Tracks"
            2 -> "Broadcasts"
            else -> null
        }
    }
}