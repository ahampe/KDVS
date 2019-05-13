package fho.kdvs.global.web

import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.extensions.urlEncoded
import fho.kdvs.global.util.HttpHelper
import org.json.JSONObject
import timber.log.Timber

enum class MusicBrainzType(val type: String) {
    RECORDING("recording"),
    RELEASE("release")
}

object MusicBrainz {

    private const val covertArtDomain = "http://coverartarchive.org/release/"
    private const val searchDomain = "https://musicbrainz.org/ws/2/"

    @JvmStatic
    fun fetchTrackInfo(track: TrackEntity): TrackEntity {
        val type = if (track.album.isNullOrBlank()) MusicBrainzType.RECORDING else MusicBrainzType.RELEASE

        var json = getMusicBrainzResponse(type,
            if (track.album.isNullOrBlank()) makeRecordingQuery(track) else makeReleaseQuery(track))

        // If no results for release, try artist + recording
        if (type == MusicBrainzType.RELEASE && isResponseEmpty(json))
            json = getMusicBrainzResponse(type, makeRecordingQuery(track))

        if (!isResponseEmpty(json)) {
            val metadata = getMetadataFromJson(json, type)
            track.hasScrapedMetadata = true

            val id = getIdFromMetadata(metadata)

            val yearStr = getRootLevelElmFromMetadataOfType<String>("date", metadata)
            if (yearStr?.length == 4)
                track.year = yearStr
                    .substring(0,4)
                    .toIntOrNull()

            if (!track.label.isNullOrBlank()) {
                track.label = getLabelFromMetadata(metadata)
            }

            if (id.isNotEmpty()) {
                // TODO: try each release ID? (on separate thread)
                val covertArtArchiveJson = getCovertArtArchiveResponse(id)
                track.imageHref = getHrefFromJson(covertArtArchiveJson)
            }
        }

        return track
    }

    /** https://wiki.musicbrainz.org/Development/JSON_Web_Service */
    private fun getMusicBrainzResponse(type: MusicBrainzType, query: String): JSONObject {
        val url = "$searchDomain${type.type}?query=$query" + "&fmt=json"
        Timber.d("GET $url")
        return HttpHelper.getJsonResponse(url)
    }

    private fun getCovertArtArchiveResponse(id: String): JSONObject {
        val url = "$covertArtDomain$id"
        Timber.d("GET $url")
        return HttpHelper.getJsonResponse(url)
    }

    private fun getIdFromMetadata(json: JSONObject?): String {
        var id = ""

        if (json?.has("id") == true)
            id = json.getString("id")

        return id
    }

    private fun getHrefFromJson(json: JSONObject): String {
        if (json.has("images")) {
            val images = json.getJSONArray("images")
            val obj = images?.getJSONObject(0)
            return obj?.getString("image") ?: ""
        }
        else return ""
    }

    private fun getMetadataFromJson(json: JSONObject?, type: MusicBrainzType): JSONObject? {
        return when(type) {
            MusicBrainzType.RECORDING -> {
                var dataObj: JSONObject? = null

                val recordings = json
                    ?.getJSONArray("recordings")

                for (i in 0 until (recordings?.length() ?: 0)) {
                    val node = recordings?.get(i) as? JSONObject
                    if (node?.has("releases") == true){
                        dataObj = node.getJSONArray("releases")
                            ?.getJSONObject(0)
                        break
                    }
                }

                dataObj
            }
            MusicBrainzType.RELEASE -> json
                ?.getJSONArray("releases")
                ?.getJSONObject(0)
        }
    }

    private inline fun <reified T> getRootLevelElmFromMetadataOfType(key: String, metadata: JSONObject?): T?{
        var elm: T? = null

        if (metadata?.has(key) == true && metadata.get(key) is T && metadata.get(key) != null)
            elm = metadata.get(key) as? T

        return elm
    }

    private fun getLabelFromMetadata(metadata: JSONObject?): String {
        var label = ""

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

    private fun isResponseEmpty(json: JSONObject): Boolean {
        return !json.has("count") || json.getInt("count") == 0
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