package jako.test.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DateTimeFormatterTest {

    @Test
    fun testIsoLocalDateRoundtrip() {
        val date = LocalDate.of(2024, 2, 29)
        val text = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
        assertEquals("2024-02-29", text)
        assertEquals(date, LocalDate.parse(text))
        assertEquals(date, LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE))
        assertEquals(date, LocalDate.from(DateTimeFormatter.ISO_LOCAL_DATE.parse("2024-02-29")))
    }

    @Test
    fun testIsoLocalTimeRoundtrip() {
        val time = LocalTime.of(10, 15, 30, 123_000_000)
        assertEquals("10:15:30.123", time.format(DateTimeFormatter.ISO_LOCAL_TIME))
        assertEquals("10:15:00", LocalTime.of(10, 15).format(DateTimeFormatter.ISO_LOCAL_TIME))
        assertEquals(LocalTime.of(10, 15), LocalTime.parse("10:15", DateTimeFormatter.ISO_LOCAL_TIME))
        assertEquals(time, LocalTime.parse("10:15:30.123", DateTimeFormatter.ISO_LOCAL_TIME))
    }

    @Test
    fun testIsoLocalDateTimeRoundtrip() {
        val ldt = LocalDateTime.of(2011, 12, 3, 10, 15, 30)
        assertEquals("2011-12-03T10:15:30", ldt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        assertEquals("2011-12-03T10:15:00", LocalDateTime.of(2011, 12, 3, 10, 15).format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        assertEquals(ldt, LocalDateTime.parse("2011-12-03T10:15:30"))
        assertEquals(ldt, LocalDateTime.parse("2011-12-03T10:15:30", DateTimeFormatter.ISO_LOCAL_DATE_TIME))
    }

    @Test
    fun testIsoInstantRoundtrip() {
        val instant = Instant.ofEpochSecond(
            LocalDateTime.of(2011, 12, 3, 10, 15, 30).toEpochSecond(ZoneOffset.UTC)
        )
        val text = DateTimeFormatter.ISO_INSTANT.format(instant)
        assertEquals("2011-12-03T10:15:30Z", text)
        assertEquals(instant, Instant.parse(text))
        assertEquals(Instant.EPOCH, Instant.parse("1970-01-01T00:00:00Z"))
        assertEquals("1970-01-01T00:00:00Z", DateTimeFormatter.ISO_INSTANT.format(Instant.EPOCH))

        val withNanos = Instant.ofEpochSecond(0, 123_000_000)
        assertEquals("1970-01-01T00:00:00.123Z", DateTimeFormatter.ISO_INSTANT.format(withNanos))
        assertEquals(withNanos, Instant.parse("1970-01-01T00:00:00.123Z"))

        val offsetParsed = Instant.parse("1970-01-01T08:00:00+08:00")
        assertEquals(Instant.EPOCH, offsetParsed)
    }

    @Test
    fun testOfPatternDateAndTime() {
        val date = LocalDate.of(2024, 3, 5)
        val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        assertEquals("2024-03-05", date.format(fmt))
        assertEquals(date, LocalDate.parse("2024-03-05", fmt))
        assertEquals("2024-3-5", date.format(DateTimeFormatter.ofPattern("yyyy-M-d")))

        val time = LocalTime.of(9, 8, 7)
        val timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss")
        assertEquals("09:08:07", time.format(timeFmt))
        assertEquals(time, LocalTime.parse("09:08:07", timeFmt))

        val ldt = LocalDateTime.of(2024, 3, 5, 9, 8, 7)
        val ldtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        assertEquals("2024-03-05T09:08:07", ldt.format(ldtFmt))
        assertEquals(ldt, LocalDateTime.parse("2024-03-05T09:08:07", ldtFmt))
        assertEquals(
            ldt,
            LocalDateTime.parse("2024-03-05 09:08:07", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        )
    }

    @Test
    fun testFormatterParseErrors() {
        assertFailsWith<DateTimeParseException> {
            DateTimeFormatter.ISO_LOCAL_DATE.parse("2024/02/29")
        }
        assertFailsWith<IllegalArgumentException> {
            DateTimeFormatter.ofPattern("yyyy-BB-dd")
        }
    }

    @Test
    fun testIsoInstantFormatsOffsetDateTime() {
        val odt = OffsetDateTime.of(LocalDateTime.of(1970, 1, 1, 8, 0), ZoneOffset.ofHours(8))
        assertEquals("1970-01-01T00:00:00Z", DateTimeFormatter.ISO_INSTANT.format(odt))
    }
}
