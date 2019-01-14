package fho.kdvs.web

import fho.kdvs.database.entities.BroadcastEntity
import fho.kdvs.database.entities.ShowEntity
import fho.kdvs.database.entities.TrackEntity
import fho.kdvs.database.models.Day
import fho.kdvs.database.models.Quarter
import fho.kdvs.extensions.enumValueOrDefault
import fho.kdvs.extensions.parseHtml
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

abstract class PageScraper<T, K> {
    val document: Document
    abstract val addList: MutableList<T>?
    abstract val updateList: MutableList<K>?
    abstract fun scrape()

    constructor(file: File) {
        document = Jsoup.parse(file, "UTF-8", "")
    }

    constructor(url: String) {
        document = Jsoup.connect(url).get()
    }
}

class SchedulePageScraper : PageScraper<ShowEntity, ShowEntity> {
    constructor(url: String) : super(url)
    constructor(file: File) : super(file)

    override val addList: MutableList<ShowEntity> = arrayListOf()
    override val updateList: MutableList<ShowEntity> = arrayListOf()

    override fun scrape() {
        document.run {
            val heading = select("h1.muted-title")?.firstOrNull()?.parseHtml()
            val title = """(\w)+ .*(\d{2,4})""".toRegex()
                .find(heading.orEmpty())?.groupValues

            val quarter = title?.getOrNull(1) ?: "Fall"

            // The schedule page is inconsistent about year numbering
            val year = title?.getOrNull(2)?.toInt()
                ?.let { if (it < 100) it + 2000 else it }
                ?: 2019

            var day = Day.SUNDAY.name

            val scheduleChildren = select("div.schedule-list > *")
            scheduleChildren.forEach { element ->
                when (element.tagName()) {
                    "h2" -> day = element.html()
                    "div" -> {
                        val imageHref = """"background-image: url\((.*)\)""".toRegex()
                            .find(element.attributes().html())
                            ?.groupValues?.getOrNull(1)?.replace("&quot;", "")

                        // Assumes that a time-slot can have arbitrarily many alternating shows
                        val (ids, names) = "<a href=\"https://kdvs.org/past-playlists/([0-9]+)\">(.*)</a>".toRegex()
                            .findAll(element.toString()).toList()
                            .map { Pair(it.groups[1]?.value?.toInt(), it.groups[2]?.value) }
                            .unzip()

                        val showTimeCaptures = parseTime(element.select(".time").first().html().trim())

                        // Don't add the show if it started on the previous day. It will be added on its starting day
                        if (isPMStart(showTimeCaptures) && element.previousElementSibling().tagName() == "h2") return@forEach
                        val timeStart = getTimeStart(showTimeCaptures)
                        val timeEnd = getTimeEnd(showTimeCaptures)

                        for (i in 0 until ids.size) {
                            val id = ids[i] ?: return@forEach
                            val showEntity = ShowEntity(
                                id = id,
                                name = names[i],
                                defaultImageHref = imageHref,
                                timeStart = timeStart,
                                timeEnd = timeEnd,
                                dayOfWeek = enumValueOrDefault(day.toUpperCase(), Day.SUNDAY),
                                quarter = enumValueOrDefault(quarter.toUpperCase(), Quarter.FALL),
                                year = year
                            )

                            addList.add(showEntity)
                        }
                    }
                }
            }
        }
    }
}

class ShowPageScraper(url: String) : PageScraper<BroadcastEntity, ShowEntity>(url) {
    override val addList: MutableList<BroadcastEntity> = arrayListOf()
    override val updateList: MutableList<ShowEntity> = arrayListOf()
    private val showId = "kdvs.org/past-playlists/([0-9]+)".toRegex()
        .find(url)?.groupValues?.getOrNull(1)?.toInt()

    override fun scrape() {
        document.run {
            val host = select("p.dj-name")?.firstOrNull()?.parseHtml()
            val genre = select("div.grid_6 p")?.getOrNull(2)?.parseHtml()
            val defaultDesc = select("div.grid_6 p")?.getOrNull(1)?.parseHtml()

            updateList.add(
                ShowEntity(
                    id = showId ?: 0,
                    host = host,
                    genre = genre,
                    defaultDesc = defaultDesc
                )
            )

            val rows = select("table.show-tracks-table tbody tr")
            rows.forEach {
                val brId = "kdvs.org/playlist-details/([0-9]+)".toRegex()
                    .find(it.toString())
                    ?.groupValues?.getOrNull(1)?.toInt()
                val date = SimpleDateFormat("MM/dd/yyyy")
                    .parse(
                        "[0-9]+/[0-9]+/[0-9]+".toRegex()
                            .find(
                                it?.select("td")
                                    ?.firstOrNull()?.parseHtml().orEmpty()
                            )
                            ?.groupValues?.firstOrNull()
                    )
                val broadcastData = BroadcastEntity(
                    broadcastId = brId ?: 0,
                    showId = showId ?: 0,
                    date = date
                )

                addList.add(broadcastData)
            }
        }
    }
}

class PlaylistPageScraper(url: String, private val fullPass: Boolean = true) :
    PageScraper<TrackEntity, BroadcastEntity>(url) {

    override val addList: MutableList<TrackEntity> = arrayListOf()
    override val updateList: MutableList<BroadcastEntity> = arrayListOf()
    private val broadcastId = "kdvs.org/playlist-details/([0-9]+)".toRegex()
        .find(url)?.groupValues?.getOrNull(1)?.toInt()

    override fun scrape() {
        document.run {
            if (fullPass) {
                // Assume description can be across arbitrarily many <p> tags following title
                var desc = ""
                var elm = select("p.dj-name")?.firstOrNull()?.nextElementSibling()
                while (elm?.tagName() != "h3") {
                    desc += elm?.parseHtml()
                    elm = elm?.nextElementSibling()
                }

                val imageElement = select("div.showcase-image")?.firstOrNull()
                val imageHref = """"background-image: url\('(.*)'\)""".toRegex()
                    .find(imageElement?.attributes()?.html().orEmpty())
                    ?.groupValues?.getOrNull(1)?.replace("&quot;", "")

                updateList.add(
                    BroadcastEntity(
                        broadcastId = broadcastId ?: 0,
                        desc = desc.trim(),
                        imageHref = imageHref?.trim()
                    )
                )
            }

            val tracks = select("table.show-tracks-table tbody tr")
            tracks.forEachIndexed { index, element ->
                val brId = broadcastId ?: return@forEachIndexed

                if (element.select("td.airbreak").isNotEmpty()) {
                    addList.add(
                        TrackEntity(
                            broadcastId = brId,
                            position = index,
                            airbreak = true
                        )
                    )
                    return@forEachIndexed
                }

                val artist = element?.select("td")?.getOrNull(0)?.parseHtml()
                val song = element?.select("td")?.getOrNull(1)?.parseHtml()
                val album = element?.select("td")?.getOrNull(2)?.parseHtml()
                val label = element?.select("td")?.getOrNull(3)?.parseHtml()
                val comment = element?.select("td")?.getOrNull(4)?.parseHtml()

                addList.add(
                    TrackEntity(
                        broadcastId = brId,
                        position = index,
                        artist = artist,
                        song = song,
                        album = album,
                        label = label,
                        comment = comment
                    )
                )
            }
        }
    }
}

//region Helper Methods
private fun parseTime(dateString: String?): List<String>? {
    // e.g. "10:30AM - 12:00PM"
    return "([0-9]+):([0-9][0-9])([AP])M - ([0-9]+):([0-9][0-9])([AP])M".toRegex()
        .find(dateString.orEmpty())
        ?.groupValues
}

private fun isPMStart(captures: List<String>?): Boolean {
    return captures?.getOrNull(3) == "P"
}

private fun isPMEnd(captures: List<String>?): Boolean {
    return captures?.getOrNull(6) == "P"
}

private fun getTimeStart(captures: List<String>?): Date {
    val isPMStart = isPMStart(captures)
    val startHour = captures?.getOrNull(1)?.toInt()
        ?.let { num -> num % 12 + (if (isPMStart) 12 else 0) }
    val startMinute = captures?.getOrNull(2)?.toIntOrNull()

    val formatter = SimpleDateFormat("HH:mm")
    return formatter.parse(startHour.toString() + ':' + startMinute.toString())
}

private fun getTimeEnd(captures: List<String>?): Date {
    val isPMEnd = isPMEnd(captures)
    val endHour = captures?.getOrNull(4)?.toInt()
        ?.let { num -> num % 12 + (if (isPMEnd) 12 else 0) }
    val endMinute = captures?.getOrNull(5)?.toIntOrNull()

    val formatter = SimpleDateFormat("HH:mm")
    return formatter.parse(endHour.toString() + ':' + endMinute.toString())
}
//endregion