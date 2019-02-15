/*
 * Copyright 2018 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package fho.kdvs.services

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.ResultReceiver
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.content.res.ResourcesCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.ExtractorMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import fho.kdvs.R
import fho.kdvs.global.database.BroadcastDao
import fho.kdvs.global.database.ShowDao
import fho.kdvs.global.extensions.albumArt
import fho.kdvs.global.extensions.from
import fho.kdvs.global.extensions.toMediaSource
import fho.kdvs.global.util.URLs
import kotlinx.coroutines.*
import java.lang.Exception
import java.net.URI
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/**
 * Class to bridge KDVS to the ExoPlayer MediaSession extension.
 * Playback preparation is handled using coroutines for background work.
 */
@Singleton
class KdvsPlaybackPreparer @Inject constructor(
    application: Application,
    private val exoPlayer: ExoPlayer,
    private val showDao: ShowDao,
    private val broadcastDao: BroadcastDao
) : MediaSessionConnector.PlaybackPreparer, CoroutineScope {

    // Use this to track and cancel the child job. There should only be one job present at a time.
    private var job: Job? = null

    private val parentJob = Job()
    override val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.IO

    private val dataSourceFactory = DefaultDataSourceFactory(
        application, Util.getUserAgent(application, application.resources.getString(R.string.app_name)), null
    )

    private val requestOptions = RequestOptions()
        .centerCrop()
        .error(R.drawable.show_placeholder)
        .fallback(R.drawable.show_placeholder)
        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)

    private val glide = Glide.with(application).applyDefaultRequestOptions(requestOptions)

    private val defaultArt: Bitmap by lazy {
        glide.asBitmap()
            .load(R.drawable.show_placeholder)
            .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
            .get()
    }

    private val liveDescriptionCompat: MediaDescriptionCompat by lazy {
        MediaDescriptionCompat.Builder()
            .setTitle(application.resources.getString(R.string.kdvs))
            .setSubtitle(application.resources.getString(R.string.live))
            .setIconBitmap(defaultArt)
            .build()
    }

    /**
     * KDVS supports preparing (and playing) from search, as well as media ID, so those
     * capabilities are declared here.
     *
     * TODO: Add support for ACTION_PREPARE and ACTION_PLAY, which mean "prepare/play something".
     */
    override fun getSupportedPrepareActions(): Long =
        PlaybackStateCompat.ACTION_PREPARE_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID or
                PlaybackStateCompat.ACTION_PREPARE_FROM_SEARCH or
                PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH

    override fun onPrepare() = Unit

    /**
     * Here, [mediaId] is either the live stream URL or is a broadcast ID string.
     * If preparing a broadcast, a bundle containing the show ID int should be sent as well, to populate the correct metadata.
     */
    override fun onPrepareFromMediaId(mediaId: String?, extras: Bundle?) {
        job?.cancel()

        job = if (mediaId in URLs.liveStream) {
            // Just play KDVS live
            prepareLive(mediaId)
        } else {
            // Play a past broadcast
            if (extras?.containsKey(SHOW_ID) == false) return
            val showId = extras?.getInt(SHOW_ID) ?: return
            val broadcastId = mediaId?.toIntOrNull() ?: return

            prepareBroadcast(broadcastId, showId)
        }
    }

    /**
     * No use for this without supporting Google Assistant
     */
    override fun onPrepareFromSearch(query: String?, extras: Bundle?) = Unit

    override fun onPrepareFromUri(uri: Uri?, extras: Bundle?) = Unit

    override fun getCommands(): Array<String>? = null

    override fun onCommand(
        player: Player?,
        command: String?,
        extras: Bundle?,
        cb: ResultReceiver?
    ) = Unit

    private fun prepareLive(streamUrl: String?) = launch {
        val liveSource = ExtractorMediaSource.Factory(dataSourceFactory)
            .setTag(liveDescriptionCompat)
            .createMediaSource(Uri.parse(streamUrl))

        withContext(Dispatchers.Main) {
            exoPlayer.prepare(liveSource)
        }
    }

    private fun prepareBroadcast(broadcastId: Int, showId: Int) = launch {
        val show = showDao.getShowById(showId)
        val broadcast = broadcastDao.getBroadcastById(broadcastId)

        // TODO glide seems to crash here despite the applied request options for fallback / error
        val art = try {
            glide.asBitmap()
                .load(broadcast.imageHref)
                .submit(NOTIFICATION_LARGE_ICON_SIZE, NOTIFICATION_LARGE_ICON_SIZE)
                .get()
        } catch (e: Exception) {
            defaultArt
        }

        val broadcastMetadata = MediaMetadataCompat.Builder()
            .from(broadcast, show)
            .apply { albumArt = art }
            .build()

        val mediaSource = broadcastMetadata.toMediaSource(dataSourceFactory)

        withContext(Dispatchers.Main) {
            exoPlayer.prepare(mediaSource)
        }
    }

    companion object {
        /** bundle key for show ID int */
        const val SHOW_ID = "SHOW_ID"
        private const val NOTIFICATION_LARGE_ICON_SIZE = 144 // px
    }
}

