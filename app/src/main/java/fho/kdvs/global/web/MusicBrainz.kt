package fho.kdvs.global.web

import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.util.HttpHelper
import org.json.JSONObject
import timber.log.Timber

/**
 * https://wiki.musicbrainz.org/Development/JSON_Web_Service
 * 
 * For our purposes, there are two relevant MusicBrainz objects: release and recording.
 * Releases correspond to an album/single/etc. Recordings are linked to tracks of a release.
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

class MusicBrainzData(
    val label: String?,
    override val albumTitle: String?,
    override val imageHref: String?,
    override val year: Int?
): ThirdPartyData()

abstract class MusicBrainz: IThirdPartyMusicAPI {

    private val covertArtDomain = "http://coverartarchive.org/release/"
    private val searchDomain = "https://musicbrainz.org/ws/2/"
    private val jsonQs = "&fmt=json"
    private val releaseQs = "inc=labels"

    abstract override fun getMusicData(title: String?, artist: String?): MusicBrainzData?

    protected fun getTopResultFromSongQuery(query: String): JSONObject? {
        val recording = getRecordingFromQuery(query)
        return getTopReleaseFromRecording(recording)
    }

    protected fun getTopResultFromAlbumQuery(query: String): JSONObject? {
        val releases = getReleasesFromQuery(query)
        return getReleaseAtPosition(0, releases)
    }

    protected fun getLabelFromTopResult(json: JSONObject?): String? {
        if (json?.has("label-info") == true){
            val labelInfo = json.getJSONArray("label-info")
            if (labelInfo.length() > 0) {
                val topLabelInfo = labelInfo.getJSONObject(0)
                if (topLabelInfo?.has("label") == true) {
                    val labelObj = topLabelInfo.getJSONObject("label")
                    if (labelObj?.has("name") == true)
                        return labelObj.getString("name")
                }
            }
        }

        return null
    }

    protected fun getYearFromTopResult(json: JSONObject?): Int? {
        val yearStr = JsonHelper.getRootLevelElmOfType<String>("date", json)
        return if ((yearStr?.length ?: 0) >= 4)
            yearStr
                ?.substring(0,4)
                ?.toIntOrNull()
        else null
    }

    /** Iterate through releases and attempt to retrieve coverArt href.*/
    protected fun getImageHrefFromReleases(json: JSONObject): String? {
        var imageHref = ""
        val releaseCount = JsonHelper.getRootLevelElmOfType<Int>("count", json) ?: 0
        // TODO: cap this at a certain limit? make preference

        (0 until releaseCount).asSequence()
            .takeWhile { imageHref.isNullOrEmpty() }
            .forEach {
                val releaseAtPosition = getReleaseAtPosition(it, json)

                releaseAtPosition?.let {
                    imageHref = getImageHrefFromRelease(releaseAtPosition) ?: ""
                }
            }

        return imageHref
    }

    protected fun getImageHrefFromRelease(json: JSONObject): String? {
        val id: String? = JsonHelper.getRootLevelElmOfType("id", json)
        val response = getCoverArtResponse(id)

        return getHrefFromCoverArtResponse(response)
    }

    protected fun getQuery(title: String, artist: String): String {
        return "\"" + title.makeStringFuzzyAndEncoded() +
                "\" AND artist:" + "\"" + artist.makeStringFuzzyAndEncoded() + "\""
    }

    private fun getRecordingFromQuery(query: String): JSONObject? {
        val url = "${searchDomain}recording?query=$query$jsonQs"
        return getMusicBrainzResponse(url)
    }

    private fun getReleasesFromQuery(query: String): JSONObject? {
        val url = "${searchDomain}release?query=$query&$releaseQs$jsonQs"
        return getMusicBrainzResponse(url)
    }

    private fun getTopReleaseFromRecording(json: JSONObject?): JSONObject? {
        if (json?.has("recordings") == true) {
            val recordings = json.getJSONArray("recordings")

            for (i in 0 until (recordings?.length() ?: 0)) {
                val node = recordings?.get(i) as? JSONObject
                if (node?.has("releases") == true){
                    val releases = node.getJSONArray("releases") // note: this node does not contain label info
                    if (releases.length() > 0) {
                        val topReleaseObj = releases?.getJSONObject(0)
                        if (topReleaseObj?.has("id") == true) {
                            return getReleaseFromMBID(topReleaseObj.getString("id"))
                        }
                    }
                }
            }
        }

        return null
    }

    private fun getReleaseFromMBID(mbid: String): JSONObject? {
        val url = "${searchDomain}release/$mbid?$releaseQs$jsonQs"
        return getMusicBrainzResponse(url)
    }

    private fun getMusicBrainzResponse(url: String): JSONObject? {
        Timber.d("GET $url")
        return HttpHelper.makeGETRequest(url)
    }

    private fun getCoverArtResponse(id: String?): JSONObject? {
        if (id == null) return null

        val url = "$covertArtDomain$id"
        Timber.d("GET $url")
        return HttpHelper.makeGETRequest(url)
    }

    private fun getHrefFromCoverArtResponse(json: JSONObject?): String? {
        var href: String?= null

        if (json?.has("images") == true) {
            val images = json.getJSONArray("images")
            val obj = images?.getJSONObject(0)
            href = obj?.getString("image") ?: ""
        }

        return href
    }

    private fun getReleaseAtPosition(position: Int, json: JSONObject?): JSONObject? {
        return if (json?.has("releases") == true &&
            json.getJSONArray("releases").length() > 0)
                json.getJSONArray("releases")
                ?.getJSONObject(position)
        else
            null
    }

    /** Append '~' to each word to make fuzzy */
    private fun String?.makeStringFuzzyAndEncoded(): String? {
        return this?.urlEncoded?.replace(" ", " ~")?.replace("+", "~+") + "~"
    }
}

class MusicBrainzAlbum: MusicBrainz() {
    override fun getMusicData(title: String?, artist: String?): MusicBrainzData? {
        if (title.isNullOrEmpty() || artist.isNullOrEmpty())
            return null

        val query = getQuery(title, artist)

        var imageHref: String? = null

        val topResult = getTopResultFromAlbumQuery(query)
            .also { json ->
                json?.let {
                    imageHref = getImageHrefFromReleases(json) ?: ""
                }
            }

        return MusicBrainzData(
            imageHref,
            JsonHelper.getRootLevelElmOfType<String>("title", topResult),
            getLabelFromTopResult(topResult),
            getYearFromTopResult(topResult)
        )
    }
}

class MusicBrainzSong: MusicBrainz() {
    override fun getMusicData(title: String?, artist: String?): MusicBrainzData? {
        if (title.isNullOrEmpty() || artist.isNullOrEmpty())
            return null

        val query = getQuery(title, artist)

        var imageHref: String? = null

        val topResult = getTopResultFromSongQuery(query)
            .also { json ->
                json?.let {
                    imageHref = getImageHrefFromRelease(json) ?: ""
                }
            }

        return MusicBrainzData(
            imageHref,
            JsonHelper.getRootLevelElmOfType<String>("title", topResult),
            getLabelFromTopResult(topResult),
            getYearFromTopResult(topResult)
        )
    }
}