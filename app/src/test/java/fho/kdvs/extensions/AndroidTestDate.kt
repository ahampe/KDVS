package fho.kdvs.extensions

import androidx.test.platform.app.InstrumentationRegistry
import com.jakewharton.threetenabp.AndroidThreeTen

/**
 * Workaround for JSR310 in instrumentation tests.
 * https://pbochenski.pl/blog/04-14-2017-testing-three-ten-abp.html
 * */
fun initAndroidThreeTen() {
    val appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    AndroidThreeTen.init(appContext)
}