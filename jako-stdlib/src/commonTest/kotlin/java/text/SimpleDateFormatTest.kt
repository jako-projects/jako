package net.bennyhuo.jako.stdlib.test

import java.text.ParseException
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SimpleDateFormatTest {

    @Test
    fun testDateFormattingAndParsing() {
        val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z")
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val date = Date(784111777000L) // Sun Nov 06 1994 08:49:37 GMT
        val formatted = sdf.format(date)
        assertEquals("Sun, 06 Nov 1994 08:49:37 UTC", formatted)

        val parsed = sdf.parse("Sun, 06 Nov 1994 08:49:37 GMT")
        assertNotNull(parsed)
        assertEquals(784111777000L, parsed.time)
    }

    @Test
    fun testRfc850Parsing() {
        val sdf = SimpleDateFormat("EEEE, dd-MMM-yy HH:mm:ss z")
        sdf.timeZone = TimeZone.getTimeZone("UTC")

        val parsed = sdf.parse("Sunday, 06-Nov-94 08:49:37 GMT")
        assertNotNull(parsed)
        assertEquals(784111777000L, parsed.time)
    }

    @Test
    fun testParseException() {
        val sdf = SimpleDateFormat("yyyy-MM-dd")
        kotlin.test.assertFailsWith<java.text.ParseException> {
            sdf.parse("invalid-date")
        }
    }

    @Test
    fun testIsoOffsetPatternsForUtcAndShanghai() {
        val pattern = "yyyy-MM-dd HH:mm:ss X XX XXX Z"
        val utc = SimpleDateFormat(pattern, Locale.US)
        utc.setTimeZone(TimeZone.getTimeZone("UTC"))
        assertEquals("1970-01-01 00:00:00 Z Z Z +0000", utc.format(Date(0)))

        val shanghai = SimpleDateFormat(pattern, Locale.US)
        shanghai.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"))
        assertEquals("1970-01-01 08:00:00 +08 +0800 +08:00 +0800", shanghai.format(Date(0)))
    }

    @Test
    fun testRfc822OffsetFollowsNewYorkDaylightTime() {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.US)
        sdf.setTimeZone(TimeZone.getTimeZone("America/New_York"))
        // 2024-07-15 12:00:00.000 EDT
        assertEquals("2024-07-15 12:00:00 -0400", sdf.format(Date(1721059200000L)))
        // 2024-01-15 12:00:00.000 EST
        assertEquals("2024-01-15 12:00:00 -0500", sdf.format(Date(1705338000000L)))
    }

    @Test
    fun testQuotedDateAmPmDayOfYearAndHourClock() {
        val sdf = SimpleDateFormat("yyyy'年'MM'月'dd'日' hh:mm a D k", Locale.US)
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        // 2024-02-29 00:00:00.000 UTC
        assertEquals("2024年02月29日 12:00 AM 60 24", sdf.format(Date(1709164800000L)))

        val afternoon = SimpleDateFormat("hh:mm a", Locale.US)
        afternoon.setTimeZone(TimeZone.getTimeZone("UTC"))
        // 2024-02-29 15:04:00.000 UTC
        assertEquals("03:04 PM", afternoon.format(Date(1709219040000L)))
    }

    @Test
    fun testIllegalPatternCharacter() {
        assertFailsWith<IllegalArgumentException> {
            SimpleDateFormat("yyyy-ii-dd", Locale.US)
        }
    }

    @Test
    fun testLenientFebruaryThirtiethRollsIntoMarch() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        sdf.setLenient(true)
        val parsed = sdf.parse("2023-02-30")
        assertNotNull(parsed)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.time = parsed
        assertEquals(2023, cal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH))
        assertEquals(2, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testStrictInvalidMonthErrorIndexThenLenientNormalization() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        sdf.setLenient(false)
        val pos = ParsePosition(0)
        assertNull(sdf.parse("2024-13-01", pos))
        assertEquals(0, pos.index)
        assertEquals(10, pos.errorIndex)

        sdf.setLenient(true)
        val parsed = sdf.parse("2024-13-01")
        assertNotNull(parsed)
        assertEquals(1735689600000L, parsed.time)
    }

    @Test
    fun testParseEmptyStringThrows() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        assertFailsWith<ParseException> {
            sdf.parse("")
        }
    }
}
