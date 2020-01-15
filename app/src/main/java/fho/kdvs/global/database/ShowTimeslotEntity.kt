package fho.kdvs.global.database

import fho.kdvs.global.enums.Quarter
import org.threeten.bp.OffsetDateTime

/** POJO for [ShowEntity] and [TimeslotEntity] join return type. */
data class ShowTimeslotEntity (
    override val id: Int,
    override var name: String? = null,
    override var host: String? = null,
    override var genre: String? = null,
    override var defaultDesc: String? = null,
    override var defaultImageHref: String? = null,
    override var quarter: Quarter? = null,
    override var year: Int? = null,
    var timeStart: OffsetDateTime? = null,
    var timeEnd: OffsetDateTime? = null
): Show()