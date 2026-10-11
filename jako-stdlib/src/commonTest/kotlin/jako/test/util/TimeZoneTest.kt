package jako.test.util

import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TimeZoneTest {

    @Test
    fun testGetTimeZone() {
        val utc = TimeZone.getTimeZone("UTC")
        assertEquals(0, utc.rawOffset)

        val gmtPlus8 = TimeZone.getTimeZone("GMT+8")
        assertEquals(8 * 3600 * 1000, gmtPlus8.rawOffset)

        val shanghai = TimeZone.getTimeZone("Asia/Shanghai")
        assertEquals(8 * 3600 * 1000, shanghai.rawOffset)

        val ny = TimeZone.getTimeZone("America/New_York")
        assertEquals(-5 * 3600 * 1000, ny.rawOffset)
    }

    @Test
    fun testAvailableIDs() {
        val ids = TimeZone.getAvailableIDs()
        assertTrue(ids.contains("UTC"))
        assertTrue(ids.contains("Asia/Shanghai"))
    }
}
