package fho.kdvs.global.web

import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.util.HttpHelper
import org.json.JSONObject
import timber.log.Timber

object MusicBrainz {

    private const val covertArtDomain = "http://coverartarchive.org/release/"
    private const val searchDomain = "https://musicbrainz.org/ws/2/"
    private const val jsonQs = "&fmt=json"
    private const val releaseQs = "inc=labels"

    private var releases: JSONObject?= null
    private var topRelease: JSONObject?= null

    @JvmStatic
    fun fetchTrackInfo(track: TrackEntity): TrackEntity {
        getReleasesFromTrack(track)

        if (topRelease == null && releases != null && !isResponseEmpty(releases)){
            track.imageHref = attemptToGetImageFromReleases()
        } else if (topRelease != null) { // MBID GET will only have one release
            val id: String? = getRootLevelElmFromJsonOfType("id", topRelease)
            val covertArtArchiveJson = getCoverArtArchiveResponse(id)
            track.imageHref = getHrefFromJson(covertArtArchiveJson)
        }

        if (track.album.isNullOrBlank())
            track.album = getRootLevelElmFromJsonOfType<String>("title", topRelease)

        if (track.label.isNullOrBlank())
            track.label = getLabelFromRelease(topRelease)

        val yearStr = getRootLevelElmFromJsonOfType<String>("date", topRelease)
        if ((yearStr?.length ?: 0) >= 4)
            track.year = yearStr
                ?.substring(0,4)
                ?.toIntOrNull()

        track.hasScrapedMetadata = true
        return track
    }

    // Try each release ID until we get an image
    private fun attemptToGetImageFromReleases(): String? {
        var imageHref: String?= null
        val releaseCount = getRootLevelElmFromJsonOfType<Int>("count", releases) ?: 0

        (0 until releaseCount).asSequence()
            .takeWhile { imageHref.isNullOrEmpty() } // TODO: cap this at a certain limit?
            .forEach {
                val releaseAtPosition = getReleaseAtPosition(it, releases)
                val id: String? = getRootLevelElmFromJsonOfType("id", releaseAtPosition)
                val covertArtArchiveJson = getCoverArtArchiveResponse(id)
                return getHrefFromJson(covertArtArchiveJson)
            }

        return null
    }

    /** https://wiki.musicbrainz.org/Development/JSON_Web_Service */
    private fun getMusicBrainzResponse(url: String): JSONObject? {
        Timber.d("GET $url")
        return HttpHelper.getJsonResponse(url)
    }

    private fun getCoverArtArchiveResponse(id: String?): JSONObject? {
        if (id == null) return null

        val url = "$covertArtDomain$id"
        Timber.d("GET $url")
        return HttpHelper.getJsonResponse(url)
    }

    private fun getHrefFromJson(json: JSONObject?): String? {
        var href: String?= null

        if (json?.has("images") == true) {
            val images = json.getJSONArray("images")
            val obj = images?.getJSONObject(0)
            href = obj?.getString("image") ?: ""
        }

        return href
    }

    private fun getReleaseAtPosition(position: Int, json: JSONObject?): JSONObject? {
        return if (json?.has("releases") == true)
            json
            .getJSONArray("releases")
            ?.getJSONObject(position)
        else
            null
    }

    private fun getRecordingFromSong(track: TrackEntity): JSONObject? {
        val query = makeRecordingQuery(track)
        val url = "${searchDomain}recording?query=$query$jsonQs"
        return getMusicBrainzResponse(url)
    }

    private fun getReleasesFromAlbum(track: TrackEntity): JSONObject? {
        val query = makeReleaseQuery(track)
        val url = "${searchDomain}release?query=$query&$releaseQs$jsonQs"
        return getMusicBrainzResponse(url)
    }

    private fun getReleaseFromMBID(mbid: String): JSONObject? {
        return getMusicBrainzResponse("${searchDomain}release/$mbid?$releaseQs$jsonQs")
    }

    private fun getReleasesFromRecording(json: JSONObject?): JSONObject? {
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

    private fun getReleaseFromSong(track: TrackEntity): JSONObject? {
        val recording = getRecordingFromSong(track)
        return getReleasesFromRecording(recording)
    }

    private fun getReleasesFromTrack(track: TrackEntity) {
        if (track.album.isNullOrBlank()) {
            topRelease = getReleaseFromSong(track)
        } else {
            releases = getReleasesFromAlbum(track)
            if (isResponseEmpty(releases)) // try song if no results for album
                topRelease = getReleaseFromSong(track)
        }
    }

    // TODO: refactor this into a general purpose JSON helper
    private inline fun <reified T> getRootLevelElmFromJsonOfType(key: String, json: JSONObject?): T?{
        var elm: T? = null

        if (json?.has(key) == true && json.get(key) is T && json.get(key) != null)
            elm = json.get(key) as? T

        return elm
    }

    private fun getLabelFromRelease(metadata: JSONObject?): String? {
        var label: String? = null

        if (metadata?.has("label-info") == true){
            val labelInfo = metadata.getJSONArray("label-info").get(0) as? JSONObject
            if (labelInfo?.has("label") == true){
                val labelObj = labelInfo.get("label") as? JSONObject
                if (labelObj?.has("name") == true)
                    label = labelObj.getString("name")
            }
        }

        return label
    }

    private fun isResponseEmpty(json: JSONObject?): Boolean {
        return json == null || !json.has("count") || json.getInt("count") == 0
    }

    private fun makeRecordingQuery(track: TrackEntity): String {
        return "\"" + track.song.makeStringFuzzyAndEncoded() +
                "\" AND artist:" + "\"" + track.artist.makeStringFuzzyAndEncoded() + "\""
    }

    private fun makeReleaseQuery(track: TrackEntity): String {
        return "\"" + track.album.makeStringFuzzyAndEncoded() +
                "\" AND artist:" + "\"" + track.artist.makeStringFuzzyAndEncoded() + "\""
    }

    // Append '~' to each word to make fuzzy
    private fun String?.makeStringFuzzyAndEncoded(): String? {
        return this?.urlEncoded?.replace(" ", " ~")?.replace("+", "~+") + "~"
    }
}