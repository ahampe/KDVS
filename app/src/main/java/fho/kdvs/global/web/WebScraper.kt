package fho.kdvs.global.web

import androidx.annotation.VisibleForTesting
import fho.kdvs.global.database.BroadcastEntity
import fho.kdvs.global.database.KdvsDatabase
import fho.kdvs.global.database.ShowEntity
import fho.kdvs.global.database.TrackEntity
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.enums.enumValueOrDefault
import fho.kdvs.global.extensions.listOfNulls
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs
import fho.kdvs.schedule.QuarterYear
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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
class WebScraperManager @Inject constructor(
    private val db: KdvsDatabase,
    private val kdvsPreferences: KdvsPreferences
) : CoroutineScope {

    private val job = Job()

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.IO

    /** A map of the currently running jobs. */
    private val urlMap = mutableMapOf<String, Job>()

    /**
     * Launches a coroutine that will scrape either the schedule grid, a show's details page, or a broadcast's details page.
     * There is no need to specify which type of page, as this will be determined by the given URL.
     *
     * This should be the preferred method for scraping in general, unless a background process needs the result of a scrape call.
     * In that case, [scrapeBlocking] should be used.
     */
    fun scrape(url: String): Job? {
        if (urlMap[url]?.isActive == true) {
            Timber.d("Already scraping url $url; exiting...")
            return null
        }

        return launch { scrapeBlocking(url) }.also {
            urlMap[url] = it
        }
    }

    /** Runs a blocking version of [scrape] and returns a [ScrapeData]. This should not be called from the main thread. */
    fun scrapeBlocking(url: String): ScrapeData? = try {
        Timber.d("Scraping: $url")
        val document = Jsoup.connect(url).get()

        when {
            url.contains("schedule-grid") -> scrapeSchedule(document)
            url.contains("past-playlists") -> scrapeShow(document)
            url.contains("playlist-details") -> scrapePlaylist(document)
            else -> throw Exception("Invalid url: $url")
        }
    } catch (e: Throwable) {
        Timber.d("Error while trying to connect: $e") // TODO reflect error in UI
        null
    } finally {
        urlMap.remove(url)
    }

    private fun scrapeSchedule(document: Document): ScheduleScrapeData {
        val heading = document.select("h1.muted-title")?.firstOrNull()?.parseHtml()
        val title = """(\w+)\s.*(\d{2,4})""".toRegex()
            .find(heading.orEmpty())?.groupValues

        val quarterName = title?.getOrNull(1)?.toUpperCase() ?: "FALL"
        val quarter = enumValueOrDefault(quarterName, Quarter.FALL)

        // The schedule page is inconsistent about year numbering
        val year = title?.getOrNull(2)?.toInt()
            ?.let { if (it < 100) it + 2000 else it }
            ?: 2019

        var day = Day.SUNDAY.name

        val showsScraped = mutableListOf<ShowEntity>()

        val scheduleChildren = document.select("div.schedule-list > *")

        var imageHrefs = mutableSetOf<String?>()

        scheduleChildren.forEach { element ->
            when (element.tagName()) {
                "h2" -> day = element.html().toUpperCase()
                "div" -> {
                    var imageHref = """background-image:\s*url\(&quot;(.*)&quot;\)""".toRegex()
                        .find(element.attributes().html())
                        ?.groupValues?.getOrNull(1)

                    // Force placeholder if duplicate image (because of kdvs.org schedule bug)
                    // exclude recurring media shows (e.g. Democracy Now)
                    // TODO: programmatic placeholder reference?
                    if (imageHref in imageHrefs && !imageHref!!.contains("library.kdvs.org/media"))
                        imageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg"
                    else
                        imageHrefs.add(imageHref)

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
                            quarter = quarter,
                            year = year
                        )

                        showsScraped.add(showEntity)
                    }
                }
            }
        }

        showsScraped.forEach { show ->
            db.showDao().updateOrInsert(show)
        }

        kdvsPreferences.lastScheduleScrape = OffsetDateTime.now().toEpochSecond()
        return ScheduleScrapeData(QuarterYear(quarter, year), showsScraped)
    }

    private fun scrapeShow(document: Document): ShowScrapeData? {
        val url = document.head().select("meta[property=og:url]").firstOrNull()?.attr("content")

        val showId = "kdvs.org/past-playlists/([0-9]+)".toRegex()
            .find(url.orEmpty())?.groupValues?.getOrNull(1)?.toInt() ?: return null

        val hostNode = document.select("p.dj-name").firstOrNull()
        val host = if (hostNode == null) "" else hostNode.parseHtml()
        val defaultDesc =
            if (hostNode?.nextElementSibling()?.tagName() == "h3") "" else (hostNode?.nextElementSibling()?.parseHtml())

        val genreHeaderNode = document.select("div.grid_6 h3:contains(Genre)")?.firstOrNull()
        val genre = if (genreHeaderNode == null) "" else genreHeaderNode.nextElementSibling()?.parseHtml()

        db.showDao().updateShowDetails(showId, host, genre, defaultDesc)

        val broadcastsScraped = mutableListOf<BroadcastEntity>()

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
                    date = TimeHelper.makeLocalDate(year, month, day)
                )

                broadcastsScraped.add(broadcastData)
            }
        }

        broadcastsScraped.forEach { broadcast ->
            db.broadcastDao().updateOrInsert(broadcast)
        }

        kdvsPreferences.setLastShowScrape(showId.toString(), OffsetDateTime.now().toEpochSecond())
        return ShowScrapeData(broadcastsScraped)
    }

    private fun scrapePlaylist(document: Document): PlaylistScrapeData? {
        val url = document.head().select("meta[property=og:url]").firstOrNull()?.attr("content")

        val broadcastId = "kdvs.org/playlist-details/([0-9]+)".toRegex()
            .find(url.orEmpty())?.groupValues?.getOrNull(1)?.toInt() ?: return null

        val tracksScraped = mutableListOf<TrackEntity>()

        document.run {
            // Assume description can be across arbitrarily many <p> tags following title
            var desc = ""
            var elm = select("p.dj-name")?.firstOrNull()?.nextElementSibling()
            while (elm?.tagName() != "h3") {
                elm?.parseHtml()?.let { desc += it }
                elm = elm?.nextElementSibling()
            }

            val imageElement = select("div.showcase-image")?.firstOrNull()
            var imageHref = """"background-image: url\('(.*)'\)""".toRegex()
                .find(imageElement?.attributes()?.html().orEmpty())
                ?.groupValues?.getOrNull(1)?.trim()?.replace("&quot;", "")

            if (imageHref == URLs.SHOW_IMAGE_PLACEHOLDER) imageHref = null

            db.broadcastDao().updateBroadcastDetails(broadcastId, desc.trim(), imageHref)

            // If the show doesn't have an imageHref, and this is the most recent broadcast, set it
            val showId = db.broadcastDao().getBroadcastById(broadcastId)?.showId
            if (showId != null && imageHref != null &&
                broadcastId == db.broadcastDao().getLatestBroadcastForShow(showId)?.broadcastId
            ) {
                val showHref = db.showDao().getShowById(showId)?.defaultImageHref
                if (showHref.isNullOrEmpty()) {
                    db.showDao().updateShowDefaultImageHref(showId, imageHref)
                }
            }

            // Because tracks have auto-generated IDs, we have to clear any already scraped tracks to avoid dupes
            db.trackDao().deleteByBroadcast(broadcastId)

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
                        TrackEntity(broadcastId = broadcastId, position = index, airbreak = true)
                } else {
                    val artist = element?.select("td")?.getOrNull(0)?.parseHtml()
                    val song = element?.select("td")?.getOrNull(1)?.parseHtml()
                    val album = element?.select("td")?.getOrNull(2)?.parseHtml()
                    val label = element?.select("td")?.getOrNull(3)?.parseHtml()
                    val comment = element?.select("td")?.getOrNull(4)?.parseHtml()

                    trackEntity = TrackEntity(
                        broadcastId = broadcastId,
                        position = index,
                        artist = artist,
                        song = song,
                        album = album,
                        label = label,
                        comment = comment
                    )
                }

                db.trackDao().insert(trackEntity)
                tracksScraped.add(trackEntity)
            }
        }

        kdvsPreferences.setLastBroadcastScrape(broadcastId.toString(), OffsetDateTime.now().toEpochSecond())
        return PlaylistScrapeData(tracksScraped)
    }

    // Helper function for scraping a mock schedule html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapeSchedule(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        scrapeSchedule(document)
    }

    // Helper function for scraping a mock show html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapeShow(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        scrapeShow(document)
    }

    // Helper function for scraping a mock show html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapePlaylist(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        scrapePlaylist(document)
    }

    companion object {
        private const val MINUTE_IN_SECONDS = 60L
        const val DEFAULT_SCRAPE_FREQ = 15L * MINUTE_IN_SECONDS
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