package fho.kdvs.schedule

import fho.kdvs.global.enums.Quarter

/** A convenience holder for a quarter-year pair. Made to hold Room DAO query results. */
data class QuarterYear(val quarter: Quarter, val year: Int) {
    override fun toString() = "${quarter.name} $year"
}