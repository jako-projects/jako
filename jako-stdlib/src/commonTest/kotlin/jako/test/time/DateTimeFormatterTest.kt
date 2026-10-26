package jako.test.time

import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
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
    fun testAdjacentNumericPatterns() {
        val compactDateFmt = DateTimeFormatter.ofPattern("yyyyMMdd")
        val d = LocalDate.of(2024, 11, 5)
        assertEquals("20241105", d.format(compactDateFmt))
        assertEquals(d, LocalDate.parse("20241105", compactDateFmt))
        assertEquals(d, LocalDate.parse("20241105", DateTimeFormatter.BASIC_ISO_DATE))

        val compactDateTimeFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS")
        val ldt = LocalDateTime.of(2024, 11, 5, 14, 30, 45, 678_000_000)
        assertEquals("20241105143045678", ldt.format(compactDateTimeFmt))
        assertEquals(ldt, LocalDateTime.parse("20241105143045678", compactDateTimeFmt))
    }

    @Test
    fun testTextMonthWeekdayAnd12HourPatterns() {
        val fmt = DateTimeFormatter.ofPattern("EEE, dd MMM yyyy hh:mm:ss a", java.util.Locale.US)
        val ldt = LocalDateTime.of(2024, 3, 15, 15, 4, 5) // 2024-03-15 is Friday
        val formatted = ldt.format(fmt)
        assertEquals("Fri, 15 Mar 2024 03:04:05 PM", formatted)
        assertEquals(ldt, LocalDateTime.parse(formatted, fmt))

        val fullFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", java.util.Locale.US)
        assertEquals("Friday, March 15, 2024", ldt.toLocalDate().format(fullFmt))
        assertEquals(ldt.toLocalDate(), LocalDate.parse("Friday, March 15, 2024", fullFmt))

        // Conflicting day-of-week ("Mon" on a Friday) must fail parsing
        assertFailsWith<DateTimeParseException> {
            LocalDate.parse("Mon, 15 Mar 2024 03:04:05 PM", fmt)
        }
    }

    @Test
    fun testOffsetZoneAndOptionalSectionPatterns() {
        val optFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'HH:mm:ss[.SSS]][XXX]")
        val dOnly = LocalDate.parse("2024-03-15", optFmt)
        assertEquals(LocalDate.of(2024, 3, 15), dOnly)

        val ldtNoMillis = LocalDateTime.parse("2024-03-15T10:20:30", optFmt)
        assertEquals(LocalDateTime.of(2024, 3, 15, 10, 20, 30), ldtNoMillis)

        val odtWithMillis = OffsetDateTime.parse("2024-03-15T10:20:30.250+08:00", optFmt)
        assertEquals(
            OffsetDateTime.of(LocalDateTime.of(2024, 3, 15, 10, 20, 30, 250_000_000), ZoneOffset.ofHours(8)),
            odtWithMillis
        )

        val zdtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss XXX VV")
        val zdt = ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, ZoneId.of("America/New_York"))
        val zdtText = zdt.format(zdtFmt)
        assertEquals("2024-07-15 12:00:00 -04:00 America/New_York", zdtText)
        assertEquals(zdt, ZonedDateTime.parse(zdtText, zdtFmt))

        // withZone formatting Instant
        val instantFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneOffset.UTC)
        assertEquals("1970-01-01 00:00:00", instantFmt.format(Instant.EPOCH))
    }

    @Test
    fun testEscapedQuotesAndInvalidPatterns() {
        val quoteFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd 'at' HH''mm 'o''clock'")
        val ldt = LocalDateTime.of(2024, 3, 15, 10, 30)
        assertEquals("2024-03-15 at 10'30 o'clock", ldt.format(quoteFmt))
        assertEquals(ldt, LocalDateTime.parse("2024-03-15 at 10'30 o'clock", quoteFmt))

        assertFailsWith<DateTimeParseException> {
            DateTimeFormatter.ISO_LOCAL_DATE.parse("2024/02/29")
        }
        assertFailsWith<IllegalArgumentException> {
            DateTimeFormatter.ofPattern("yyyy-BB-dd")
        }
        assertFailsWith<IllegalArgumentException> {
            DateTimeFormatter.ofPattern("yyyy-MM-dd 'unclosed")
        }
        assertFailsWith<IllegalArgumentException> {
            DateTimeFormatter.ofPattern("yyyy-MM-dd [unclosed")
        }
        assertFailsWith<IllegalArgumentException> {
            DateTimeFormatter.ofPattern("yyyy-MM-dd ]unmatched")
        }
        assertFailsWith<IllegalArgumentException> {
            DateTimeFormatter.ofPattern("VVV")
        }
    }

    @Test
    fun testIsoInstantFormatsOffsetDateTime() {
        val odt = OffsetDateTime.of(LocalDateTime.of(1970, 1, 1, 8, 0), ZoneOffset.ofHours(8))
        assertEquals("1970-01-01T00:00:00Z", DateTimeFormatter.ISO_INSTANT.format(odt))
    }

    @Test
    fun testLocalizedFormattingAndWithZoneOffsetOverride() {
        val ldt = LocalDateTime.of(2024, 3, 15, 14, 30, 0)
        for (locale in listOf(java.util.Locale.US, java.util.Locale.CHINA, java.util.Locale.GERMANY, java.util.Locale.FRANCE, java.util.Locale.JAPAN)) {
            val fmt = DateTimeFormatter.ofPattern("yyyy MMMM dd EEEE HH:mm", locale)
            val formatted = fmt.format(ldt)
            assertEquals(ldt, LocalDateTime.parse(formatted, fmt), "Round-trip failed for locale $locale: $formatted")
        }

        // withZone on a formatter with an explicit offset should convert instant to target zone
        val utcFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX").withZone(ZoneId.of("UTC"))
        val parsed = ZonedDateTime.parse("2024-07-15T14:00:00+02:00", utcFmt)
        assertEquals(LocalDateTime.of(2024, 7, 15, 12, 0, 0), parsed.toLocalDateTime())
        assertEquals(ZoneOffset.UTC, parsed.offset)
        assertEquals(ZoneId.of("UTC"), parsed.zone)
    }
}

