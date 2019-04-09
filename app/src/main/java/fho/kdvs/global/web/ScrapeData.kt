package fho.kdvs.global.web

import fho.kdvs.global.database.*
import fho.kdvs.schedule.QuarterYear

/**
 * Data classes containing all the data that was scraped by [WebScraperManager].
 *
 * In general, these classes don't need to be used. The scraper inserts items into the database, and the database can
 * be treated as the source of truth throughout the app. However, if a background process needs some information from
 * the website immediately, it can use one of these classes to access the data the scraper found.
 */
sealed class ScrapeData
data class ScheduleScrapeData(val quarterYear: QuarterYear, val shows: List<ShowEntity>) : ScrapeData()
data class ShowScrapeData(val broadcasts: List<BroadcastEntity>) : ScrapeData()
data class PlaylistScrapeData(val tracks: List<TrackEntity>) : ScrapeData()
data class TopMusicScrapeData(val topMusicItems: List<TopMusicEntity>) : ScrapeData()
data class ContactScrapeData(val staff: List<StaffEntity>) : ScrapeData()
data class NewsScrapeData(val news: List<NewsEntity>) : ScrapeData()

