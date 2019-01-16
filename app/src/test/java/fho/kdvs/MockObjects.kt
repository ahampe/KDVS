package fho.kdvs

import fho.kdvs.model.database.entities.BroadcastEntity
import fho.kdvs.model.database.entities.ShowEntity
import fho.kdvs.model.database.entities.TrackEntity
import fho.kdvs.model.database.models.Day
import fho.kdvs.model.database.models.Quarter

object MockObjects {
    val scheduleShows: List<ShowEntity> by lazy {
        listOf(
            ShowEntity(
                id = 5235,
                name = "Neonate--New Life",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("00:00"),
                timeEnd = TestUtils.makeDateFromTime("02:00"),
                dayOfWeek = Day.SUNDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5238,
                name = "Island Radio Cafe",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.SUNDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5240,
                name = "Crossing Continents",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.SUNDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5239,
                name = "Cross-cultural Currents",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.SUNDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5280,
                name = "The Suicide Watch",
                defaultImageHref = "https://goo.gl/images/nkV7oU",
                timeStart = TestUtils.makeDateFromTime("22:00"),
                timeEnd = TestUtils.makeDateFromTime("01:00"),
                dayOfWeek = Day.MONDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5289,
                name = "Club 903",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("14:30"),
                timeEnd = TestUtils.makeDateFromTime("16:30"),
                dayOfWeek = Day.TUESDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5257,
                name = "la buena onda",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:30"),
                timeEnd = TestUtils.makeDateFromTime("12:00"),
                dayOfWeek = Day.WEDNESDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5370,
                name = "field trip",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("10:30"),
                timeEnd = TestUtils.makeDateFromTime("12:00"),
                dayOfWeek = Day.WEDNESDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5320,
                name = "How You Jewin'?",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("09:00"),
                timeEnd = TestUtils.makeDateFromTime("09:30"),
                dayOfWeek = Day.THURSDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5331,
                name = "Democracy Now!",
                defaultImageHref = "https://library.kdvs.org/media/show/images/unnamed.png",
                timeStart = TestUtils.makeDateFromTime("12:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.THURSDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5333,
                name = "Democracy Now!",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("12:00"),
                timeEnd = TestUtils.makeDateFromTime("13:00"),
                dayOfWeek = Day.FRIDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5355,
                name = "1000 Points of Fright",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("20:00"),
                timeEnd = TestUtils.makeDateFromTime("22:00"),
                dayOfWeek = Day.FRIDAY,
                quarter = Quarter.WINTER,
                year = 2019
            ),
            ShowEntity(
                id = 5364,
                name = "UnAbbreviated Country",
                defaultImageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg",
                timeStart = TestUtils.makeDateFromTime("16:00"),
                timeEnd = TestUtils.makeDateFromTime("18:00"),
                dayOfWeek = Day.SATURDAY,
                quarter = Quarter.WINTER,
                year = 2019
            )
        )
    }

    val showDetails: List<ShowEntity> by lazy {
        listOf(
            ShowEntity(
                id = 5235,
                host = "Punk Roge",
                genre = "Hardcore, Old School Punk, Punk, Street Punk",
                defaultDesc = "2 Hours of punk rock."
            ),
            ShowEntity(
                id = 5238,
                host = "Gary B. Goode",
                genre = "Reggae",
                defaultDesc = "New releases plus surprises. Reggae ten at ten. Latino/a, Hawaiian music. African as well. Jazz too."
            ),
            ShowEntity(
                id = 5240,
                host = "Gil Medovoy",
                genre = "International",
                defaultDesc = "International/World"
            ),
            ShowEntity(
                id = 5239,
                host = "Mindy",
                genre = "International, Reggae",
                defaultDesc = "Reggae + African"
            ),
            ShowEntity(
                id = 5280,
                host = "Ophelia Necro",
                genre = "",
                defaultDesc = ""
            ),
            ShowEntity(
                id = 5289,
                host = "HIV",
                genre = "Electronic",
                defaultDesc = "FKA The Swear Jar."
            ),
            ShowEntity(
                id = 5257,
                host = "dj pan francés",
                genre = "Latino Alternativo",
                defaultDesc = "reminder to stop sleeping on latin american rock."
            ),
            ShowEntity(
                id = 5370,
                host = "crimson wave & Mood Ring",
                genre = "Eclectic",
                defaultDesc = "i made you a mixtape!"
            ),
            ShowEntity(
                id = 5320,
                host = "Mathilda",
                genre = "Public Affairs",
                defaultDesc = "Chat show about Jewish life + culture"
            ),
            ShowEntity(
                id = 5331,
                host = "Staff",
                genre = "News",
                defaultDesc = ""
            ),
            ShowEntity(
                id = 5333,
                host = "Staff",
                genre = "News",
                defaultDesc = ""
            ),
            ShowEntity(
                id = 5355,
                host = "Pirate of the High Frequenseas",
                genre = "Metal",
                defaultDesc = "Metal and all of it's ugly children"
            ),
            ShowEntity(
                id = 5364,
                host = "Fuzzy Mic",
                genre = "Bluegrass, Blues, Country, Folk",
                defaultDesc = "Music from the country is a big world. And we'll listen to all of it. Country, bluegrass, folk, and blues all come from the same roots. Tune in and hear modern, classic, and roots country."
            )
        )
    }

    val showBroadcasts = listOf(
        BroadcastEntity(broadcastId = 51090, showId = 5326, date = TestUtils.makeDate("01/18/2019")),
        BroadcastEntity(broadcastId = 51089, showId = 5326, date = TestUtils.makeDate("01/11/2019"))
        )

    val broadcasts: List<BroadcastEntity> by lazy {
        listOf(
            BroadcastEntity(
                broadcastId = 50771,
                showId = 5280,
                date = TestUtils.makeDate("01/14/2019")
            ),
            BroadcastEntity(
                broadcastId = 50770,
                showId = 5280,
                date = TestUtils.makeDate("01/07/2019")
            )
        )
    }

    val broadcastDetails: List<BroadcastEntity> by lazy {
        listOf(
            BroadcastEntity(
                broadcastId = 50771,
                showId = null,
                desc = "",
                imageHref = "https://library.kdvs.org/static/core/images/kdvs-image-placeholder.jpg"
            ),
            BroadcastEntity(
                broadcastId = 50506,
                showId = null,
                desc = "",
                imageHref = "https://library.kdvs.org/media/show/images/Neonate_Logo.jpg"
            ),
            BroadcastEntity(
                broadcastId = 51695,
                showId = null,
                desc = "what did you expect...",
                imageHref = "https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/intermediary/f/00f6ed51-f1ee-44ed-8e6f-ab16c838cef0/d13ay51-188a8475-6dbd-4027-b871-45e2bd57495b.jpg"
            )
        )
    }

    val trackDetails: List<TrackEntity> by lazy {
        listOf(
            TrackEntity(
                broadcastId = 50771,
                artist = "HG Lewis",
                song = "Official warning",
                album = "Eyepopping Sounds of HG Lewis",
                label = "Birdman",
                comment = "",
                position = 0
            ),
            TrackEntity(
                broadcastId = 50771,
                artist = "Neurosis",
                song = "The Eye of Every Storm",
                album = "The Eye of Every Storm",
                label = "Neurot",
                comment = "",
                position = 1
            ),
            TrackEntity(
                broadcastId = 50771,
                artist = "Scott Kelly and the the Road Home",
                song = "We Let the Hell Come",
                album = "The Forgiven Ghost in Me",
                label = "Neurot",
                comment = "",
                position = 2
            ),
            TrackEntity(
                broadcastId = 50771,
                airbreak = true,
                position = 3
            ),
            TrackEntity(
                broadcastId = 50771,
                artist = "Dark Buddha Rising",
                song = "Mahathgata I",
                album = "II",
                label = "Neurot",
                comment = "",
                position = 4
            ),
            TrackEntity(
                broadcastId = 50771,
                artist = "Chrch",
                song = "Portals",
                album = "The Light Will Consume Us All",
                label = "Neurot",
                comment = "",
                position = 5
            )
        )
    }
}