package fho.kdvs

import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.models.Day
import fho.kdvs.model.database.models.Quarter
import java.util.*

object DbTestUtils {
    fun createShow() = ShowEntity(
        id = 1888,
        name = "Pick a Bale of Cotton",
        host = "Leadbelly",
        genre = "Blues",
        defaultDesc = "Goodnight Irene",
        defaultImageHref = "http://www.leadbelly.com/cotton.jpg",
        timeStart = Date(1_000L),
        timeEnd = Date(2_000L),
        dayOfWeek = Day.SUNDAY,
        quarter = Quarter.SPRING,
        year = 1943
    )

    fun createBroadcast() = BroadcastEntity(
        broadcastId = 290,
        showId = 1888,
        desc = "A stompin' good time with Blind Lemon",
        date = Date(5_000_00L),
        imageHref = "http://www.leadbelly.com/blindlemon.jpg"
    )
}