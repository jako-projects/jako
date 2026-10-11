package net.bennyhuo.jako.stdlib.test

import java.text.SimpleDateFormat
import java.text.ParsePosition

import java.util.Date
import java.util.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

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
}
