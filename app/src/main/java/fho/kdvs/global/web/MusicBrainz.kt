package fho.kdvs.global.web

import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.util.HttpHelper
import org.apache.commons.text.StringEscapeUtils
import org.json.JSONObject
import org.w3c.dom.Document

object MusicBrainz {

    private const val covertArtDomain = "http://coverartarchive.org/release/"
    private const val searchDomain = "https://musicbrainz.org/ws/2/"

    @JvmStatic
    fun fetchTrackInfo(track: TrackEntity): TrackEntity {
        val type = if (track.album.isNullOrBlank()) "recording" else "release"

        val doc = getMusicBrainzResponse(type,
            if (track.album.isNullOrBlank()) makeRecordingQuery(track) else makeReleaseQuery(track))
        track.metadata = doc

        val id = getIdFromXml(doc, type)

        val covertArtArchiveJson = getCovertArtArchiveResponse(id)

        track.imageHref = getHrefFromJson(covertArtArchiveJson)

        return track
    }

    /** https://musicbrainz.org/doc/Development/XML_Web_Service/Version_2/Search */
    private fun getMusicBrainzResponse(type: String, query: String): Document {
        val url = "$searchDomain$type?query=$query".htmlEncode()
        return HttpHelper.getDocumentResponse(url)
    }

    private fun getCovertArtArchiveResponse(id: String): JSONObject {
        val url = "$covertArtDomain$id".htmlEncode()
        return HttpHelper.getJsonResponse(url)
    }

    private fun getIdFromXml(doc: Document, type: String): String {
        return doc.getElementById(type).getAttribute("id")
    }

    private fun getHrefFromJson(json: JSONObject): String {
        return json.get("image").toString()
    }

    private fun makeRecordingQuery(track: TrackEntity): String {
        return "\"" + track.song + "\" AND artist:" + track.artist
    }

    private fun makeReleaseQuery(track: TrackEntity): String {
        return "\"" + track.album + "\" AND artist:" + track.artist
    }

    private fun String.htmlEncode(): String {
        return StringEscapeUtils.escapeHtml4(this)
    }
}