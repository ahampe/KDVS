package fho.kdvs.global.web

import androidx.annotation.VisibleForTesting
import fho.kdvs.global.database.*
import fho.kdvs.global.enums.Day
import fho.kdvs.global.enums.Quarter
import fho.kdvs.global.enums.enumValueOrDefault
import fho.kdvs.global.extensions.fromHtml
import fho.kdvs.global.extensions.listOfNulls
import fho.kdvs.global.preferences.KdvsPreferences
import fho.kdvs.global.util.TimeHelper
import fho.kdvs.global.util.URLs.SHOW_IMAGE_PLACEHOLDER
import fho.kdvs.schedule.QuarterYear
import fho.kdvs.topmusic.TopMusicType
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.parser.Parser
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
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
     * Launches a coroutine that will scrape a web page.
     * There is no need to specify which type of page, as this will be determined by the given URL.
     *
     * This should be the preferred method for scraping in general, unless a background process needs the result of a scrape call.
     * In that case, [scrapeBlocking] should be used.
     */
    fun scrape(url: String): Job? {
        if (kdvsPreferences.offlineMode == true) {
            Timber.d("Offline mode blocked scrape")
            return null
        } else if (urlMap[url]?.isActive == true) {
            Timber.d("Already scraping url $url; exiting...")
            return null
        }

        return launch { scrapeBlocking(url) }.also {
            urlMap[url] = it
        }
    }

    /** Runs a blocking version of [scrape] and returns a [ScrapeData]. This should not be called from the main thread. */
    private fun scrapeBlocking(url: String): ScrapeData? = try {
        if (kdvsPreferences.offlineMode == true) {
            Timber.d("Offline mode blocked scrape")
            null
        } else {
            Timber.d("Scraping: $url")
            val document = Jsoup.connect(url).get()

            when {
                url.contains("schedule-grid") -> scrapeSchedule(document)
                url.contains("past-playlists") -> scrapeShow(document, url)
                url.contains("playlist-details") -> scrapePlaylist(document, url)
                url.contains("contact") -> scrapeStaff(document)
                url.contains("news") -> scrapeNews(document, url)
                url.contains("top-") -> scrapeTopMusic(document, url)
                url.contains("fundraiser") -> scrapeFundraiser(document)
                else -> throw Exception("Invalid url: $url")
            }
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
        val timeslotsScraped = mutableListOf<TimeslotEntity>()

        val scheduleChildren = document.select("div.schedule-list > *")

        val imageHrefs = mutableSetOf<String?>()

        scheduleChildren.forEach { element ->
            when (element.tagName()) {
                "h2" -> day = element.parseHtml()?.toUpperCase() ?: ""
                "div" -> {
                    var imageHref = """background-image:\s*url\(&quot;(.*)&quot;\)""".toRegex()
                        .find(element.attributes().html())
                        ?.groupValues?.getOrNull(1)

                    // Force placeholder if duplicate image (because of kdvs.org schedule bug)
                    // exclude recurring media shows (e.g. Democracy Now)
                    // TODO: programmatic placeholder reference?
                    if (imageHref in imageHrefs && !imageHref!!.contains("library.kdvs.org/media"))
                        imageHref = SHOW_IMAGE_PLACEHOLDER
                    else
                        imageHrefs.add(imageHref)

                    // Assumes that a time-slot can have arbitrarily many alternating shows
                    val (ids, names) = "<a href=\"https://kdvs.org/past-playlists/([0-9]+)\">(.*)</a>".toRegex()
                        .findAll(element.toString()).toList()
                        // force unwrap is okay here; if there is a match, there will be groups
                        .map { Pair(it.groups[1]!!.value.toInt(), it.groups[2]!!.value) }
                        .unzip()

                    val showTimeCaptures =
                        parseTime(element.select(".time").first().parseHtml()?.trim())?.drop(1)

                    val (startTime, startAmpm, endTime, endAmpm) = showTimeCaptures
                        ?: listOfNulls(4)

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
                        val show = ShowEntity (
                            id = id,
                            name = name.trim(),
                            defaultImageHref = imageHref,
                            quarter = quarter,
                            year = year
                        )

                        val timeslot = TimeslotEntity (
                            showId = id,
                            timeStart = timeStart,
                            timeEnd = timeEnd
                        )

                        showsScraped.add(show)
                        timeslotsScraped.add(timeslot)
                    }
                }
            }
        }

        showsScraped.forEach {
            db.showDao().updateOrInsert(it)
        }

        timeslotsScraped.forEach {
            db.timeslotDao().insert(it)
        }

        kdvsPreferences.lastScheduleScrape = TimeHelper.getNow().toEpochSecond()
        return ScheduleScrapeData(QuarterYear(quarter, year), showsScraped, timeslotsScraped)
    }

    private fun scrapeShow(document: Document, url: String?): ShowScrapeData? {
        val showId = "kdvs.org/past-playlists/([0-9]+)".toRegex()
            .find(url.orEmpty())?.groupValues?.getOrNull(1)?.toInt() ?: return null

        val hostNode = document.select("p.dj-name").firstOrNull()
        val host = if (hostNode == null) "" else hostNode.parseHtml()
        val defaultDesc =
            if (hostNode?.nextElementSibling()?.tagName() == "h3") "" else (hostNode?.nextElementSibling()?.parseHtml())

        val genreHeaderNode = document.select("div.grid_6 h3:contains(Genre)")?.firstOrNull()
        val genre =
            if (genreHeaderNode == null) "" else genreHeaderNode.nextElementSibling()?.parseHtml()

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

        kdvsPreferences.setLastShowScrape(showId.toString(), TimeHelper.getNow().toEpochSecond())
        return ShowScrapeData(broadcastsScraped)
    }

    private fun scrapePlaylist(document: Document, url: String?): PlaylistScrapeData? {
        val broadcastId = "kdvs.org/playlist-details/([0-9]+)".toRegex()
            .find(url.orEmpty())?.groupValues?.getOrNull(1)?.toInt() ?: return null

        val tracksScraped = mutableListOf<TrackEntity>()

        document.run {
            // Assume description can be across arbitrarily many <p> tags following title
            var desc = ""
            var elm = select("p.dj-name")?.firstOrNull()?.nextElementSibling()
            while (elm?.tagName() != "h3") {
                elm?.parseHtml()?.let { desc += it + "\n" }
                elm = elm?.nextElementSibling()
            }
            desc = desc.trim()

            val imageElement = select("div.showcase-image")?.firstOrNull()
            var imageHref = """"background-image: url\('(.*)'\)""".toRegex()
                .find(imageElement?.attributes()?.html().orEmpty())
                ?.groupValues?.getOrNull(1)?.trim()?.replace("&quot;", "")

            if (imageHref == SHOW_IMAGE_PLACEHOLDER) imageHref = null

            db.broadcastDao().updateBroadcastDetails(broadcastId, desc.trim(), imageHref)

            // If the show doesn't have an imageHref, and this is the most recent broadcast, set it
            val showId = db.broadcastDao().getBroadcastById(broadcastId)?.showId
            if (showId != null && imageHref != null &&
                broadcastId == db.broadcastDao().getLatestBroadcastForShow(showId)?.broadcastId
            ) {
                val showHref = db.showDao().getShowTimeslotById(showId)?.defaultImageHref
                if (showHref.isNullOrEmpty()) {
                    db.showDao().updateShowDefaultImageHref(showId, imageHref)
                }
            }

            // Because tracks have auto-generated IDs, we have to clear any already scraped tracks to avoid dupes
            db.trackDao().deleteByBroadcast(broadcastId)
            // TODO: this deletion may pose a conflict if user has favorited prior tracks

            // filter out empty playlists
            val tracks = select("table.show-tracks-table tbody tr").filter { t ->
                t.children().count() != 1 ||
                        !t.html().toUpperCase().contains("EMPTY PLAYLIST")
            }
            tracks.forEachIndexed { index, element ->
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

        kdvsPreferences.setLastBroadcastScrape(
            broadcastId.toString(),
            TimeHelper.getNow().toEpochSecond()
        )
        return PlaylistScrapeData(tracksScraped)
    }

    private fun scrapeTopMusic(document: Document, url: String?): TopMusicScrapeData? {
        val type = if (url?.toLowerCase()?.contains("adds") == true)
            TopMusicType.ADD
        else TopMusicType.ALBUM

        val topMusicItemsScraped = mutableListOf<TopMusicEntity>()

        document.run {
            val dates = select("h2.top-title")
            dates.forEach { element ->
                val dateCaptures = parseDate(element.toString())
                val month = dateCaptures?.getOrNull(1)?.toInt()
                val day = dateCaptures?.getOrNull(2)?.toInt()
                val year = dateCaptures?.getOrNull(3)?.toInt()
                val date = LocalDate.of(year ?: 0, month ?: 0, day ?: 0)

                val albums = element.nextElementSibling().select("li")
                if (albums.size >= 5) { // ignore blank lists
                    albums.forEachIndexed { index, elm ->
                        val captures = "(.+)<br>.*>(.+)<.*>.*\\((.+)\\)".toRegex()
                            .find(elm.parseHtml() ?: "")
                            ?.groupValues
                        val artist = captures?.getOrNull(1)?.toString()
                        val album = captures?.getOrNull(2)?.toString()
                        val label = captures?.getOrNull(3)?.toString()

                        if (captures != null) {
                            topMusicItemsScraped.add(
                                TopMusicEntity(
                                    weekOf = date,
                                    type = type,
                                    position = index,
                                    artist = artist,
                                    album = album,
                                    label = label
                                )
                            )
                        }
                    }
                }
            }
        }

        topMusicItemsScraped.forEach { topMusic ->
            db.topMusicDao().insert(topMusic)
        }

        when (type) {
            TopMusicType.ADD -> kdvsPreferences.lastTopFiveAddsScrape =
                TimeHelper.getNow().toEpochSecond()
            TopMusicType.ALBUM -> kdvsPreferences.lastTopThirtyAlbumsScrape =
                TimeHelper.getNow().toEpochSecond()
        }

        return TopMusicScrapeData(topMusicItemsScraped)
    }

    private fun scrapeStaff(document: Document): StaffScrapeData? {
        val staffScraped = mutableListOf<StaffEntity>()

        document.run {
            val staff = select("table.contact-table tbody tr")
            staff.forEach { element ->
                val leftContainer = element.select("td").firstOrNull()
                val middleContainer = element.select("td").getOrNull(1)
                val rightContainer = element.select("td").getOrNull(2)

                val name = element.select("td strong")?.text().let {
                    if (it.isNullOrEmpty())
                        element.select("td b")?.text()
                    else
                        it
                }?.replace("& ", "&\n")
                val position = leftContainer?.ownText()
                val email = leftContainer?.select("a")
                    ?.map { it.ownText() }
                    ?.reduce { acc, s -> acc + s }
                val duties = middleContainer?.ownText()
                val officeHours = rightContainer?.parseHtml()?.fromHtml()?.trim()

                staffScraped.add(
                    StaffEntity(
                        name = name,
                        position = position,
                        email = email,
                        duties = duties,
                        officeHours = officeHours
                    )
                )
            }

            db.staffDao().deleteAll()

            staffScraped.forEach { s ->
                db.staffDao().insert(s)
            }
        }

        kdvsPreferences.lastStaffScrape = TimeHelper.getNow().toEpochSecond()

        return StaffScrapeData(staffScraped)
    }

    private fun scrapeNews(document: Document, url: String?): NewsScrapeData? {
        val articlesScraped = mutableListOf<NewsEntity>()

        lateinit var lastDateScraped: LocalDate

        document.run {
            val articles = select("article")
            articles.forEach { article ->
                val aCell = article.select("h2 a")
                val articleHref = aCell.attr("href")
                val title = aCell.getOrNull(0).parseHtml()

                val titleDiv = article.select("div.post-meta").first()
                val author = titleDiv.select("span").firstOrNull().parseHtml()

                val dateCaptures = parseDate(titleDiv.parseHtml())
                val month = dateCaptures?.getOrNull(1)?.toInt()
                val day = dateCaptures?.getOrNull(2)?.toInt()
                val year = dateCaptures?.getOrNull(3)?.toInt()
                val date = LocalDate.of(year ?: 0, month ?: 0, day ?: 0)
                lastDateScraped = date

                val mainBodyDiv = article.select("div.post-content, div.entry-content")
                var imageHref: String? = null
                val image = article.select("img").firstOrNull()
                image?.let {
                    imageHref = image.attr("src")
                }

                val body = mainBodyDiv.select("div.excerpt, div.entry-content")
                    .map { it.parseHtml() }
                    .joinToString("\n")
                    .replace("<br>", "\n")
                    .processHtml()

                articlesScraped.add(
                    NewsEntity(
                        title = title,
                        author = author,
                        body = body,
                        date = date,
                        articleHref = articleHref,
                        imageHref = imageHref
                    )
                )
            }

            articlesScraped.forEach { article ->
                db.newsDao().insert(article)
            }

            // if the last article on the page is within the past 6 months, scrape the next page as well
            if (LocalDateTime.now(Clock.systemUTC()).minusMonths(6) <= lastDateScraped.atTime(
                    0,
                    0
                )
            ) {
                val currentPage = "page/([0-9]+)".toRegex()
                    .find(url.toString())
                    ?.groupValues
                    ?.getOrNull(1)
                    ?: "1"

                val newsUrl = "page/([0-9]+)".toRegex()
                    .replace(url.toString(), "")

                scrapeBlocking(newsUrl + "page/" + (currentPage.toInt() + 1).toString())
            }
        }

        kdvsPreferences.lastNewsScrape = TimeHelper.getNow().toEpochSecond()

        return NewsScrapeData(articlesScraped)
    }

    private fun scrapeFundraiser(document: Document): FundraiserScrapeData? {
        lateinit var fundraiser: FundraiserEntity

        document.run {
            val info = select("div.hero-info").firstOrNull()

            val dateCaptures = """(\w+)\s([0-9]+)-([0-9]+),\s([0-9][0-9][0-9][0-9])""".toRegex()
                .find(info?.html().toString())
                ?.groupValues

            val dayStart = dateCaptures?.getOrNull(2)?.toInt()
            val dayEnd = dateCaptures?.getOrNull(3)?.toInt()
            val year = dateCaptures?.getOrNull(4)?.toInt()
            val monthStr = dateCaptures?.getOrNull(1)
            val month = TimeHelper.monthStrToInt(monthStr)

            val dateStart = LocalDate.of(year ?: 0, month, dayStart ?: 0)
            val dateEnd = LocalDate.of(year ?: 0, month, dayEnd ?: 0)

            val moneyCaptures = """Goal[:\s$]*([\d,]+)\sCurrent[:\s$]*([\d,]+)""".toRegex()
                .find(info?.select("p:not([class])")?.text() ?: "")
                ?.groupValues
            val goal = moneyCaptures
                ?.getOrNull(1)
                ?.replace(",", "")
                ?.toInt()
            val current = moneyCaptures
                ?.getOrNull(2)
                ?.replace(",", "")
                ?.toInt()

            if (dateCaptures != null) {
                fundraiser = FundraiserEntity(
                    goal = goal,
                    current = current,
                    dateStart = dateStart,
                    dateEnd = dateEnd
                )
                db.fundraiserDao().deleteAll()
                db.fundraiserDao().insert(fundraiser)
            }
        }

        kdvsPreferences.lastFundraiserScraper = TimeHelper.getNow().toEpochSecond()

        return FundraiserScrapeData(fundraiser)
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
        val url = document.head().select("meta[property=og:url]").firstOrNull()?.attr("content")
        scrapeShow(document, url)
    }

    // Helper function for scraping a mock playlist html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapePlaylist(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        val url = document.head().select("meta[property=og:url]").firstOrNull()?.attr("content")
        scrapePlaylist(document, url)
    }

    // Helper function for scraping a mock staff html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapeTopMusic(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        val url = document.head().select("meta[property=og:url]").firstOrNull()?.attr("content")
            ?: document.select("link[rel='canonical']").attr("href")
        scrapeTopMusic(document, url)
    }

    // Helper function for scraping a mock staff html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapeStaff(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        scrapeStaff(document)
    }

    // Helper function for scraping a mock news html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapeNews(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        val url = document.head().select("meta[property=og:url]").firstOrNull()?.attr("content")
        scrapeNews(document, url)
    }

    // Helper function for scraping a mock fundraiser html file
    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    fun scrapeFundraiser(file: File) {
        val document = Jsoup.parse(file, "UTF-8", "")
        scrapeFundraiser(document)
    }

    companion object {
        private const val MINUTE_IN_SECONDS = 60L
        const val DEFAULT_SCRAPE_FREQ = 15L * MINUTE_IN_SECONDS
        const val DAILY_SCRAPE_FREQ = 24L * 60L * MINUTE_IN_SECONDS
        const val WEEKLY_SCRAPE_FREQ = 7L * DAILY_SCRAPE_FREQ
    }
}

//region Helper Methods
private fun parseDate(dateString: String?): List<String>? {
    return "([0-9]+)/([0-9]+)/([0-9]+)".toRegex()
        .find(dateString.orEmpty())
        ?.groupValues
}

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

/** Strip HTML tags from a string. */
private fun String?.stripHtml(): String? {
    if (this == null) return null

    return this.replace("<[^>]*>".toRegex(), "")
}

/** Strip html tags, trim, remove inner-string spaces near newlines, unescape html */
private fun String?.processHtml(): String? {
    if (this == null) return null

    return Parser.unescapeEntities(
        this.stripHtml()
            ?.trim()
            ?.replace("""\s*\n\s*""".toRegex(), "\n"),
        false
    )
}
//endregion