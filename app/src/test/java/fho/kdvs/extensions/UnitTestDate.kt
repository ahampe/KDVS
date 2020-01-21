package fho.kdvs.extensions

import org.threeten.bp.zone.TzdbZoneRulesProvider
import org.threeten.bp.zone.ZoneRulesProvider

/**
 * Workaround for JSR310 in unit tests.
 * https://pbochenski.pl/blog/04-14-2017-testing-three-ten-abp.html
 * */
fun Any.initThreeTen() {
    if (ZoneRulesProvider.getAvailableZoneIds().isEmpty()) {
        val stream = this.javaClass.classLoader!!.getResourceAsStream("TZDB.dat")
        stream.use(::TzdbZoneRulesProvider).apply {
            ZoneRulesProvider.registerProvider(this)
        }
    }
}