package fho.kdvs.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import dagger.android.support.DaggerFragment
import fho.kdvs.databinding.FragmentHomeBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.SharedViewModel
import fho.kdvs.news.ContactsAdapter
import fho.kdvs.news.NewsArticlesAdapter
import fho.kdvs.news.TopMusicAdapter
import kotlinx.android.synthetic.main.fragment_home.*
import timber.log.Timber
import javax.inject.Inject

class HomeFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory
    private lateinit var viewModel: HomeViewModel

    private var newsArticlesAdapter: NewsArticlesAdapter? = null
    private var topAddsAdapter: TopMusicAdapter? = null
    private var topAlbumsAdapter: TopMusicAdapter? = null
    private var contactsAdapter: ContactsAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(HomeViewModel::class.java)
            .also {it.fetchHomeData()}

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentHomeBinding.inflate(inflater, container, false)
        val sharedViewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(SharedViewModel::class.java)

        binding.apply {
            sharedVm = sharedViewModel
        }

        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newsArticlesAdapter = NewsArticlesAdapter {
            Timber.d("Clicked ${it.item}")
        }

        newsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = newsArticlesAdapter
        }
        
        topAddsAdapter = TopMusicAdapter { 
            Timber.d("Clicked ${it.item}")
        }
        
        topAddsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = topAddsAdapter
        }

        topAlbumsAdapter = TopMusicAdapter {
            Timber.d("Clicked ${it.item}")
        }

        topAlbumsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = topAlbumsAdapter
        }
        
        contactsAdapter = ContactsAdapter {
            Timber.d("Clicked ${it.item}")
        }

        contactsRecycler.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
            adapter = contactsAdapter
        }
    }

    private fun subscribeToViewModel() {
        viewModel.newsArticles.observe(this, Observer { articles ->
            Timber.d("Got articles: $articles")
            newsArticlesAdapter?.onNewsChanged(articles)
        })

        viewModel.topMusicAdds.observe(this, Observer { adds ->
            Timber.d("Got adds: $adds")
            topAddsAdapter?.onTopAddsChanged(adds)
        })

        viewModel.topMusicAlbums.observe(this, Observer { albums ->
            Timber.d("Got albums: $albums")
            topAlbumsAdapter?.onTopAlbumsChanged(albums)
        })

        viewModel.contacts.observe(this, Observer { contacts ->
            Timber.d("Got contacts: $contacts")
            contactsAdapter?.onContactsChanged(contacts)
        })
    }
}

