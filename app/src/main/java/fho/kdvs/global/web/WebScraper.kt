package fho.kdvs.global.web

import android.annotation.SuppressLint
import androidx.annotation.VisibleForTesting
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.KdvsDatabase
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.enums.enumValueOrDefault
import fho.kdvs.global.extensions.listOfNulls
import fho.kdvs.global.util.TimeHelper
import kotlinx.coroutines.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.threeten.bp.OffsetDateTime
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext

/** This class will handle the scraping for each web page and will insert items into the database one by one. */
@Singleton
class WebScraperManager @Inject constructor(private val db: KdvsDatabase) : CoroutineScope {
    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    fun scrape(url: String) {
        try {
            launch {
                val document = Jsoup.connect(url).get()

                when {
                    url.contains("schedule-grid") -> scrapeSchedule(document)
                    url.contains("past-playlists") -> scrapeShow(document)
                    url.contains("playlist-details") -> scrapePlaylist(document)
                    else -> throw Exception("Invalid url: $url")
                }
            }
        } catch (e: Throwable) {
            Timber.d("Error while trying to connect: $e") // TODO reflect error in UI
        }
    }

    private fun scrapeSchedule(document: Document) = launch {
        val heading = document.select("h1.muted-title")?.firstOrNull()?.parseHtml()
        val title = """(\w+)\s.*(\d{2,4})""".toRegex()
            .find(heading.orEmpty())?.groupValues

        val quarter = title?.getOrNull(1)?.toUpperCase() ?: "FALL"

        // The schedule page is inconsistent about year numbering
        val year = title?.getOrNull(2)?.toInt()
            ?.let { if (it < 100) it + 2000 else it }
            ?: 2019

        var day = Day.SUNDAY.name

        val scheduleChildren = document.select("div.schedule-list > *")

        scheduleChildren.forEach { element ->
            when (element.tagName()) {
                "h2" -> day = element.html().toUpperCase()
                "div" -> {
                    val imageHref = """background-image:\s*url\((.*)\)""".toRegex()
                        .find(element.attributes().html())
                        ?.groupValues?.getOrNull(1)?.replace("&quot;", "")

                    // Assumes that a time-slot can have arbitrarily many alternating shows
                    val (ids, names) = "<a href=\"https://kdvs.org/past-playlists/([0-9]+)\">(.*)</a>".toRegex()
                        .findAll(element.toString()).toList()
                        // force unwrap is okay here; if there is a match, there will be groups
                        .map { Pair(it.groups[1]!!.value.toInt(), it.groups[2]!!.value) }
                        .unzip()

                    val showTimeCaptures = parseTime(element.select(".time").first().html().trim())?.drop(1)

                    val (startTime, startAmpm, endTime, endAmpm) = showTimeCaptures ?: listOfNulls(4)

                    val timeStart = makeTime(
                        time = startTime,
                        ampm = startAmpm,
                        day = enumValueOrDefault(day, Day.SUNDAY)
                    )

                    var timeEnd = makeTime(
                        time = endTime,
                        ampm = endAmpm,
                        day = enumValueOrDefault(day, Day.SUNDAY)
                    )

                    // Special case where show extends to or beyond midnight: add a day to timeEnd
                    if (timeEnd != null && timeStart != null && timeEnd < timeStart) {
                        timeEnd = TimeHelper.addDay(timeEnd)
                    }

                    for ((name, id) in names.zip(ids)) {
                        val showEntity = ShowEntity(
                            id = id,
                            name = name.trim(),
                            defaultImageHref = imageHref,
                            timeStart = timeStart,
                            timeEnd = timeEnd,
                            quarter = enumValueOrDefault(quarter, Quarter.FALL),
                            year = year
                        )

                        db.showDao().insert(showEntity)
                    }
                }
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun scrapeShow(document: Document) = launch {
        val url = document.head().select("meta[property=og:url]").firstOrNull()?.attr("content")

        val showId = "kdvs.org/past-playlists/([0-9]+)".toRegex()
            .find(url.orEmpty())?.groupValues?.getOrNull(1)?.toInt() ?: return@launch

        val hostNode = document.select("p.dj-name").firstOrNull()
        val host = if (hostNode == null) "" else hostNode.parseHtml()
        val defaultDesc = if (hostNode?.nextElementSibling()?.tagName() == "h3") ""
        else (hostNode?.nextElementSibling()?.parseHtml())
        val genreHeaderNode = document.select("div.grid_6 h3:contains(Genre)")?.firstOrNull()
        val genre = if (genreHeaderNode == null) "" else genreHeaderNode.nextElementSibling()?.parseHtml()

        db.showDao().updateShowInfo(showId, host, genre, defaultDesc)

        // Show page may not have playlists (at the beginning of a quarter)
        if (document.select("table.show-tracks-table").html().contains("a href")) {
            val rows = document.select("table.show-tracks-table tbody tr")
            rows.forEach { row ->
                val brId = "kdvs.org/playlist-details/([0-9]+)".toRegex()
                    .find(row.toString())
                    ?.groupValues?.getOrNull(1)?.toInt()

                val dateCaptures = """(\d+)/(\d+)/(\d+)""".toRegex()
                    .find(row?.select("td")?.firstOrNull()?.parseHtml().orEmpty())
                    ?.groupValues?.drop(1)

                val (month, day, year) = dateCaptures ?: listOfNulls(3)

                val broadcastData = BroadcastEntity(
                    broadcastId = brId ?: 0,
                    showId = showId,
                    date = TimeHelper.makeLocalDate(
                        "${year?.padStart(4, '0')}" +
                                "-${month?.padStart(2, '0')}" +
                                "-${day?.padStart(2, '0')}"
                    )
                )

                db.broadcastDao().insert(broadcastData)
            }
        }
    }

    private fun scrapePlaylist(document: Document) = launch {
        val url = document.head().select("meta[property=og:url]").firstOrNull()?.attr("content")

        val broadcastId = "kdvs.org/playlist-details/([0-9]+)".toRegex()
            .find(url.orEmpty())?.groupValues?.getOrNull(1)?.toInt()

        document.run {
            // Assume description can be across arbitrarily many <p> tags following title
            var desc = ""
            var elm = select("p.dj-name")?.firstOrNull()?.nextElementSibling()
            while (elm?.tagName() != "h3") {
                elm?.parseHtml()?.let { desc += it }
                elm = elm?.nextElementSibling()
            }

            val imageElement = select("div.showcase-image")?.firstOrNull()
            val imageHref = """"background-image: url\('(.*)'\)""".toRegex()
                .find(imageElement?.attributes()?.html().orEmpty())
                ?.groupValues?.getOrNull(1)?.replace("&quot;", "")

            db.broadcastDao().updateBroadcast(broadcastId, desc.trim(), imageHref?.trim())

            // filter out empty playlists
            val tracks = select("table.show-tracks-table tbody tr").filter { t ->
                t.children().count() != 1 ||
                        !t.html().toUpperCase().contains("EMPTY PLAYLIST")
            }
            tracks.forEachIndexed { index, element ->
                val brId = broadcastId ?: return@forEachIndexed

                val trackEntity: TrackEntity
                if (element.select("td.airbreak").isNotEmpty()) {
                    trackEntity =
                        TrackEntity(broadcastId = brId, position = index, airbreak = true)
                } else {
                    val artist = element?.select("td")?.getOrNull(0)?.parseHtml()
                    val song = element?.select("td")?.getOrNull(1)?.parseHtml()
                    val album = element?.select("td")?.getOrNull(2)?.parseHtml()
                    val label = element?.select("td")?.getOrNull(3)?.parseHtml()
                    val comment = element?.select("td")?.getOrNull(4)?.parseHtml()

                    trackEntity = TrackEntity(
                        broadcastId = brId,
                        position = index,
                        artist = artist,
                        song = song,
                        album = album,
                        label = label,
                        comment = comment
                    )
                }

                db.trackDao().insert(trackEntity)
            }
        }
    }

    // Helper function for scraping a mock schedule html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapeSchedule(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        runBlocking { scrapeSchedule(document).join() }
    }

    // Helper function for scraping a mock show html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapeShow(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        runBlocking { scrapeShow(document).join() }
    }

    // Helper function for scraping a mock show html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapePlaylist(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        runBlocking { scrapePlaylist(document).join() }
    }

}

//region Helper Methods
private fun parseTime(dateString: String?): List<String>? {
    // e.g. "10:30AM - 12:00PM"
    return """([0-9]{1,2}:[0-9]{2})\s?([AP]M)\s?-\s?([0-9]{1,2}:[0-9]{2})\s?([AP]M)""".toRegex()
        .find(dateString.orEmpty())
        ?.groupValues
}

private fun makeTime(time: String?, ampm: String?, day: Day?): OffsetDateTime? {
    if (time == null || ampm == null || day == null) return null

    return TimeHelper.makeWeekTime12h("$time $ampm", day)
}
//endregion