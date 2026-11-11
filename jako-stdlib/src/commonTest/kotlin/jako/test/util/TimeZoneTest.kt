package jako.test.util

import java.util.Date
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun testUtcIdOffsetAndDaylight() {
        val utc = TimeZone.getTimeZone("UTC")
        assertEquals("UTC", utc.getID())
        assertEquals(0, utc.getOffset(0L))
        assertFalse(utc.useDaylightTime())
        assertFalse(utc.inDaylightTime(Date(0)))
    }

    @Test
    fun testUtcCaseAndEquals() {
        val utc = TimeZone.getTimeZone("UTC")
        val lower = TimeZone.getTimeZone("utc")
        val mixed = TimeZone.getTimeZone("Utc")
        assertEquals("GMT", lower.getID())
        assertEquals(0, lower.rawOffset)
        assertEquals("GMT", mixed.getID())
        assertEquals(0, mixed.rawOffset)
        assertFalse(utc.equals(lower))
        assertFalse(utc.equals(TimeZone.getTimeZone("GMT")))
        assertTrue(utc.equals(TimeZone.getTimeZone("UTC")))
    }

    @Test
    fun testUnknownIdsFallBackToGmt() {
        val empty = TimeZone.getTimeZone("")
        assertEquals("GMT", empty.getID())
        assertEquals(0, empty.rawOffset)

        val unknown = TimeZone.getTimeZone("Not/AZone")
        assertEquals("GMT", unknown.getID())
        assertEquals(0, unknown.rawOffset)

        val malformed = TimeZone.getTimeZone("GMT+abc")
        assertEquals("GMT", malformed.getID())
        assertEquals(0, malformed.rawOffset)
    }

    @Test
    fun testUtcPlusEightIsNotAnOffset() {
        val zone = TimeZone.getTimeZone("UTC+8")
        assertEquals("GMT", zone.getID())
        assertEquals(0, zone.rawOffset)
    }

    @Test
    fun testCustomGmtOffsets() {
        val plus8 = TimeZone.getTimeZone("GMT+8")
        assertEquals("GMT+08:00", plus8.getID())
        assertEquals(28800000, plus8.rawOffset)
        assertEquals(plus8.rawOffset, plus8.getOffset(0L))
        assertFalse(plus8.useDaylightTime())

        val plus8Padded = TimeZone.getTimeZone("GMT+08:00")
        assertEquals("GMT+08:00", plus8Padded.getID())
        assertEquals(28800000, plus8Padded.rawOffset)
        assertEquals(plus8Padded.rawOffset, plus8Padded.getOffset(0L))
        assertFalse(plus8Padded.useDaylightTime())

        val minus5 = TimeZone.getTimeZone("GMT-5")
        assertEquals("GMT-05:00", minus5.getID())
        assertEquals(-18000000, minus5.rawOffset)
        assertEquals(minus5.rawOffset, minus5.getOffset(0L))
        assertFalse(minus5.useDaylightTime())

        val plus530 = TimeZone.getTimeZone("GMT+5:30")
        assertEquals("GMT+05:30", plus530.getID())
        assertEquals(19800000, plus530.rawOffset)
        assertEquals(plus530.rawOffset, plus530.getOffset(0L))
        assertFalse(plus530.useDaylightTime())
    }

    @Test
    fun testAsiaShanghaiNoDaylight() {
        val shanghai = TimeZone.getTimeZone("Asia/Shanghai")
        assertFalse(shanghai.useDaylightTime())
        assertEquals(28800000, shanghai.getOffset(0L))
        assertFalse(shanghai.inDaylightTime(Date(0)))
    }

    @Test
    fun testNewYorkDaylightOffsets() {
        val ny = TimeZone.getTimeZone("America/New_York")
        assertTrue(ny.useDaylightTime())
        assertEquals(-18000000, ny.rawOffset)

        val winter = Date(1705338000000L)
        assertFalse(ny.inDaylightTime(winter))
        assertEquals(-18000000, ny.getOffset(1705338000000L))

        val summer = Date(1721059200000L)
        assertTrue(ny.inDaylightTime(summer))
        assertEquals(-14400000, ny.getOffset(1721059200000L))
        assertEquals(-18000000, ny.rawOffset)
    }

    @Test
    fun testAvailableIdsByRawOffset() {
        val eastern = TimeZone.getAvailableIDs(-18000000)
        assertTrue(eastern.contains("America/New_York"))
        assertFalse(eastern.contains("Asia/Shanghai"))

        val china = TimeZone.getAvailableIDs(28800000)
        assertTrue(china.contains("Asia/Shanghai"))
    }
}
