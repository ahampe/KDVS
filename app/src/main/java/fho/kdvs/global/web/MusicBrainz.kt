package fho.kdvs.global.web

import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.util.HttpHelper
import fho.kdvs.global.web.MusicBrainz.makeStringFuzzyAndEncoded
import kotlinx.serialization.json.Json
import kotlinx.serialization.parse
import org.json.JSONObject
import timber.log.Timber

/**
 * https://wiki.musicbrainz.org/Development/JSON_Web_Service
 * 
 * For our purposes, there are two relevant MusicBrainz objects: release and recording.
 * Release correspond to an album/single/etc. Recording are linked to tracks of a release.
 * 
 * You can retrieve data from MusicBrainz with a direct MBID query, or through searches.
 * 
 * If we are forming a query from a song title, we first obtain the recording JSON response,
 * then perform a release GET for the MBID contained within.
 * 
 * If we are forming a query from an album title, we perform a release search GET and return
 * the top result of the response.
 * 
 * Release objects contain an id corresponding to data on covertartarchive.org,
 * so we perform another GET there to obtain the imageHref, if it exists.
 * 
 * Note: the topmost release result may not necessarily be linked to an image on covertartarchive,
 * and the nth result may.
 **/


@kotlinx.serialization.UnstableDefault
object MusicBrainz {
    private const val covertArtDomain = "http://coverartarchive.org/release/"
    private const val searchDomain = "https://musicbrainz.org/ws/2/"
    private const val jsonQs = "&fmt=json"
    private const val releaseQs = "inc=labels"

    fun searchFromAlbum(title: String?, artist: String?): MusicBrainzReleaseData? {
        if (title.isNullOrBlank() || artist.isNullOrBlank())
            return null

        val query = getQuery(title, artist)
        val url = getReleaseUrlFromQuery(query)
        val response = HttpHelper.makeGETRequest(url)

        response?.let {
            return Json.nonstrict.parse(MusicBrainzReleaseData.serializer(), response)
        }

        return null
    }

    fun searchFromSong(title: String?, artist: String?): MusicBrainzReleaseData? {
        if (title.isNullOrBlank() || artist.isNullOrBlank())
            return null

        val query = getQuery(title, artist)
        val url = getRecordingUrlFromQuery(query)
        val response = HttpHelper.makeGETRequest(url)

        response?.let {
            val recordingData = Json.nonstrict.parse(MusicBrainzRecordingData.serializer(), response) as? MusicBrainzRecordingData
            val mbid = recordingData
                ?.recordings?.firstOrNull()
                ?.releases?.firstOrNull()
                ?.id

            mbid?.let {
                val mbidUrl = getReleaseUrlFromMBID(mbid)
                val mbidResponse = HttpHelper.makeGETRequest(mbid)

                mbidResponse?.let {
                    return Json.nonstrict.parse(MusicBrainzReleaseData.serializer(), mbidResponse)
                }
            }
        }

        return null
    }

    fun getCoverArtImage(id: String?): String? {
        val data = getCoverArtData(id)
        return data?.images?.firstOrNull()?.image
    }

    private fun getCoverArtData(id: String?): CoverArtArchiveData? {
        if (id == null) return null

        val url = "$covertArtDomain$id"
        Timber.d("GET $url")

        val response = HttpHelper.makeGETRequest(url)
        response?.let {
            return Json.nonstrict.parse(CoverArtArchiveData.serializer(), response)
        }

        return null
    }

    private fun getQuery(title: String, artist: String): String {
        return "\"" + title.makeStringFuzzyAndEncoded() +
                "\" AND artist:" + "\"" + artist.makeStringFuzzyAndEncoded() + "\""
    }

    private fun getReleaseUrlFromQuery(query: String): String {
        return "${searchDomain}release?query=$query&$releaseQs$jsonQs"
    }

    private fun getRecordingUrlFromQuery(query: String): String {
        return "${searchDomain}recording?query=$query$jsonQs"
    }

    private fun getReleaseUrlFromMBID(mbid: String): String? {
        return "${searchDomain}release/$mbid?$releaseQs$jsonQs"
    }

    /** Append '~' to each word to make fuzzy */
    private fun String?.makeStringFuzzyAndEncoded(): String? {
        return this?.urlEncoded?.replace(" ", " ~")?.replace("+", "~+") + "~"
    }
}