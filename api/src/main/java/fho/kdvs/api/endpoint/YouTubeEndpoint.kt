package fho.kdvs.api.endpoint

import fho.kdvs.api.raw.video.YouTubeVideoSearchResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface YouTubeEndpoint {

    /**
     * Searches for the given video on YouTube and returns the top result.
     */
    @GET("youtube/v3/search")
    suspend fun searchVideos(@Header("Accept") accept: String = "application/json",
                             @Query("q") query: String,
                             @Query("maxResults") max: Int = 1,
                             @Query("part") part: String = "id",
                             @Query("type") type: String = "video",
                             @Query("key") key: String = YOUTUBE_API_KEY
    ): Response<YouTubeVideoSearchResponse>

    companion object {

        internal const val BASE_URL = "https://www.googleapis.com"

        const val YOUTUBE_API_KEY = "AIzaSyDvsEcwtR8NNoOCNm7BRkioMhUN_u_FZK4"
    }
}