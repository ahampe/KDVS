package fho.kdvs.model.database.models

import java.util.*

data class Broadcast(
    val broadcastId: Int?,
    val showId: Int?,
    var desc: String?,
    var date: Date?,
    var imageHref: String?
)
