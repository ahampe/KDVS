package fho.kdvs

import fho.kdvs.model.Day
import fho.kdvs.model.Quarter
import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity
import fho.kdvs.util.TimeHelper
import org.threeten.bp.Instant
import org.threeten.bp.ZoneOffset

object DbTestUtils {
    fun createShows() = listOf(
        ShowEntity(
            id = 1888,
            name = "Pick a Bale of Cotton",
            host = "Leadbelly",
            genre = "Blues",
            defaultDesc = "Goodnight Irene",
            defaultImageHref = "http://www.leadbelly.com/cotton.jpg",
            timeStart = TimeHelper.makeWeekTime24h("01:00", Day.SUNDAY),
            timeEnd = TimeHelper.makeWeekTime24h("02:00", Day.SUNDAY),
            quarter = Quarter.SPRING,
            year = 1943
        ),
        ShowEntity(
            id = 1889,
            name = "Tristram Shandy",
            host = "Laurence Sterne",
            genre = "Rabelaisian",
            defaultDesc = "The Absolute Madman",
            defaultImageHref = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/16/Laurence_Sterne_by_Sir_Joshua_Reynolds.jpg/800px-Laurence_Sterne_by_Sir_Joshua_Reynolds.jpg",
            timeStart = TimeHelper.makeWeekTime24h("22:00", Day.SATURDAY),
            timeEnd = TimeHelper.makeWeekTime24h("01:00", Day.SUNDAY),
            quarter = Quarter.SPRING,
            year = 1943
        )
    )

    fun createBroadcasts() = listOf(
        BroadcastEntity(
            broadcastId = 290,
            showId = 1888,
            descr = "A stompin' good time with Blind Lemon",
            date = TimeHelper.makeLocalDate("1943-01-01"),
            imageHref = "http://www.leadbelly.com/blindlemon.jpg"
        ),
        BroadcastEntity(
            broadcastId = 291,
            showId = 1888,
            descr = "The Great Machine",
            date = TimeHelper.makeLocalDate("1943-01-08"),
            imageHref = "http://sfprod.shikadi.net/pic/timss.png"
        )
    )

    fun createTracks() = listOf(
        TrackEntity(
            trackId = 1,
            broadcastId = 290,
            position = 0,
            artist = "The Limeliters",
            song = "Country Music's Got A Way of Making Me Feel Free",
            album = "Singing For The Fun"
        ),
        TrackEntity(
            trackId = 2,
            broadcastId = 290,
            position = 1,
            airbreak = true
        ),
        TrackEntity(
            trackId = 3,
            broadcastId = 290,
            position = 2,
            artist = "Byrd, Jonathan",
            song = "Tractor Pull",
            album = "Jonathan Byrd & The Pickup Cowboys",
            label = "Waterbug"
        ),
        TrackEntity(
            trackId = 4,
            broadcastId = 290,
            position = 3,
            artist = "Dolly Parton",
            song = "Blue Smoke",
            album = "Blue Smoke"
        )
    )
}