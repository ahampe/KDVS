package fho.kdvs.api.service

import fho.kdvs.api.endpoint.YouTubeEndpoint
import fho.kdvs.api.mapped.YouTubeVideo
import fho.kdvs.api.mapper.YouTubeMapper
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Client-facing singleton that wraps the Retrofit endpoint for YouTube API requests. Automatically
 * handles client authentication.
 */
@Singleton
class YouTubeService @Inject constructor(
    private val endpoint: YouTubeEndpoint
) : BaseService() {

    private val mapper = YouTubeMapper()

    /**
     * Returns top YouTube video search result (if any) from query.
     */
    suspend fun findVideoAsync(artist: String?, title: String?): Deferred<YouTubeVideo?>? =
        try {
            supervisorScope {
                async {
                    if (artist.isNullOrEmpty() || title.isNullOrEmpty()) return@async null

                    val response = endpoint.searchVideos(query = "$artist $title")

                    if (response.isSuccessful) {
                        mapper.video(response.body())
                    } else {
                        when (response.code()) {
                            400 -> {
                                // Bad request
                                // TODO
                                return@async null
                            }
                            403 -> {
                                // Forbidden. Likely exceeded quota
                                // TODO
                                return@async null
                            }
                            else -> {
                                Timber.d("Error searching YouTube for $artist - $title")
                                Timber.d(response.message())
                                response.errorBody()?.toString()?.let { Timber.d(it) }
                                return@async null
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Timber.e("Error finding YouTube video: $e")
            null
        }
}