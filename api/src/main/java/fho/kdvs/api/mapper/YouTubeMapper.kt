package fho.kdvs.api.mapper

import fho.kdvs.api.mapped.YouTubeVideo
import fho.kdvs.api.raw.video.YouTubeVideoSearchResponse

/**
 * Class which maps YouTube API responses into client-usable classes.
 */
class YouTubeMapper {

    fun video(response: YouTubeVideoSearchResponse?): YouTubeVideo? {
        return response?.items?.firstOrNull()?.id?.let {
            YouTubeVideo(id = it.id)
        }
    }
}