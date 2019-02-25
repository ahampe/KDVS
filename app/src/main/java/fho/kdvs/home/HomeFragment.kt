package fho.kdvs.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerFragment
import fho.kdvs.viewmodel.KdvsViewModel
import fho.kdvs.R
import fho.kdvs.databinding.*
import fho.kdvs.global.KdvsViewModelFactory
import kotlinx.android.synthetic.main.cell_show_current.view.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.*
import org.threeten.bp.OffsetDateTime
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext

class HomeFragment : DaggerFragment(), CoroutineScope {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    private lateinit var viewModel: KdvsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(requireActivity(), vmFactory)
            .get(KdvsViewModel::class.java)
            .also {
                it.fetchShows()
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        exoPlayerView.player = viewModel?.exoPlayer

        launch {
            //TODO: Account for the final show of a year
            val now = OffsetDateTime.now()
            val currentShowData = viewModel.getShowAtTime(now)
            val nextShowTimeStart = now
                .withHour(currentShowData.timeEnd?.hour ?: now.hour)
                ?.withYear(now.year)
            val nextShowData = viewModel.getShowAtTime(nextShowTimeStart
                ?: now)

            withContext(Dispatchers.Main) {
                Glide.with(currentShow)
                    .applyDefaultRequestOptions(
                        RequestOptions()
                            .error(R.drawable.show_placeholder)
                            .apply(RequestOptions.centerCropTransform())
                    )
                    .load(currentShowData?.defaultImageHref)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(currentShow.showImage)

                val currentBinding = CellShowCurrentBinding.bind(currentShow)
                currentBinding.apply {
                    clickListener = clickListener
                    show = currentShowData
                }
                currentBinding.executePendingBindings()

                val nextBinding = CellShowNextBinding.bind(nextShow)
                nextBinding.apply {
                    show = nextShowData
                }
                nextBinding.executePendingBindings()
            }
        }

        // TODO: schedulers to load next show at timeEnd
    }
}

