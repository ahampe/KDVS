package fho.kdvs.web

interface WebScraperFactory {
    fun callFromUrl(url: String?, fullPass: Boolean= true): PageScraper<*,*>
}

class StandardWebScraperFactory : WebScraperFactory {
    override fun callFromUrl(url: String?, fullPass: Boolean): PageScraper<*,*> =
        when {
            url.isNullOrEmpty() -> throw Exception("Provided a null or empty url.")
            url.contains("schedule-grid") -> SchedulePageScraper(url)
            url.contains("past-playlists") -> ShowPageScraper(url)
            url.contains("playlist-details") && fullPass -> PlaylistPageScraper(url)
            url.contains("playlist-details") && !fullPass -> PlaylistPageScraper(url, false)
            else -> throw Exception("Invalid url: $url")
        }
}