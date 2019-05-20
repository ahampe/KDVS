package fho.kdvs.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.request.RequestOptions
import dagger.android.support.DaggerFragment
import fho.kdvs.R
import fho.kdvs.databinding.FragmentPlayerBinding
import fho.kdvs.global.KdvsViewModelFactory
import fho.kdvs.global.MainActivity
import fho.kdvs.global.ui.PlayerPaletteRequestListener
import fho.kdvs.global.util.ImageHelper
import fho.kdvs.global.util.TimeHelper
import kotlinx.android.synthetic.main.fragment_player.*
import timber.log.Timber
import javax.inject.Inject

class PlayerFragment : DaggerFragment() {
    @Inject
    lateinit var vmFactory: KdvsViewModelFactory

    private lateinit var viewModel: PlayerViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this, vmFactory)
            .get(PlayerViewModel::class.java)
            .also { it.initialize() }

        subscribeToViewModel()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val binding = FragmentPlayerBinding.inflate(inflater, container, false)
            .apply { vm = viewModel }
        binding.lifecycleOwner = this

        val activity = activity as? MainActivity
        activity?.toggleBottomNavAndPlayerBar(false)

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()

        val activity = this.activity as? MainActivity
        activity?.toggleBottomNavAndPlayerBar(true)
    }

    private fun subscribeToViewModel() {
        viewModel.broadcastLiveData.observe(this, Observer { broadcast ->
            Timber.d("got currently playing broadcast: $broadcast")
            viewModel.showLiveData.observe(this, Observer { show ->
                Timber.d("got currently playing show: $show")
                showName.text = show.name
                showHost.text = show.host

                if ((show.defaultImageHref ?: "").isNotEmpty()) {
                    val parent = playing_image.parent as ConstraintLayout
                    Glide.with(playing_image)
                        .asBitmap()
                        .apply(
                            RequestOptions()
                                .error(R.drawable.show_placeholder)
                                .apply(RequestOptions.centerCropTransform())
                        )
                        .transition(BitmapTransitionOptions.withCrossFade())
                        .listener(
                            PlayerPaletteRequestListener(playing_image, parent)
                        )
                        .into(playing_image)
                }

                if (TimeHelper.isShowBroadcastLive(show, broadcast))
                    configureLiveExoPlayer()
                else
                    configureArchiveExoPlayer()
            })
        })
    }

    private fun configureArchiveExoPlayer() {

    }

    private fun configureLiveExoPlayer() {
        // set info icon
    }
}