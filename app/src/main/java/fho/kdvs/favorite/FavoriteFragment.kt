package fho.kdvs.favorite

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import fho.kdvs.R
import fho.kdvs.global.BaseFragment
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import kotlinx.android.synthetic.main.fragment_favorite.*
import javax.inject.Inject

const val POS_TRACKS = 0
const val POS_BROADCASTS = 1

/**
 * Fragment with a pager containing [FavoriteTrackFragment] and [FavoriteBroadcastFragment].
 */
class FavoriteFragment : BaseFragment() {

    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var favoritePagerAdapter: FavoritePagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedViewModel = ViewModelProviders.of(this, vmFactory)
            .get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_favorite, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        favoritePagerAdapter = FavoritePagerAdapter(childFragmentManager)
        favoritePager.adapter = favoritePagerAdapter

        tabLayout.setupWithViewPager(favoritePager)
    }

    enum class SortDirection(val type: String) {
        ASC("asc"),
        DES("des")
    }

    enum class SortType {
        RECENT,
        DATE,
        SHOW,
        ARTIST,
        ALBUM,
        TRACK
    }
}
