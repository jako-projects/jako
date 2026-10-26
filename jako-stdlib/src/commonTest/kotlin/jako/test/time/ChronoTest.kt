package jako.test.time

import java.time.DateTimeException
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.Month
import java.time.OffsetDateTime
import java.time.Period
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeParseException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ChronoTest {

    @Test
    fun testEpochDayZeroIsUnixEpoch() {
        val epoch = LocalDate.of(1970, 1, 1)
        assertEquals(0L, epoch.toEpochDay())
        assertEquals(epoch, LocalDate.ofEpochDay(0))
        assertEquals("1970-01-01", epoch.toString())
    }

    @Test
    fun testLeapYearFebruary29() {
        val leap = LocalDate.of(2024, 2, 29)
        assertEquals(2024, leap.year)
        assertEquals(2024, leap.getYear())
        assertEquals(2, leap.monthValue)
        assertEquals(Month.FEBRUARY, leap.month)
        assertEquals(Month.FEBRUARY, leap.getMonth())
        assertEquals(29, leap.dayOfMonth)
        assertEquals(29, leap.getDayOfMonth())
        assertTrue(leap.isLeapYear())

        assertFailsWith<DateTimeException> {
            LocalDate.of(2023, 2, 29)
        }
        assertEquals(LocalDate.of(2025, 2, 28), leap.plusYears(1))
    }

    @Test
    fun testPlusMonthsEndOfMonth() {
        assertEquals(LocalDate.of(2011, 2, 28), LocalDate.of(2011, 1, 31).plusMonths(1))
        assertEquals(LocalDate.of(2024, 2, 29), LocalDate.of(2024, 1, 31).plusMonths(1))
        assertEquals(LocalDate.of(2011, 3, 31), LocalDate.of(2011, 1, 31).plusMonths(2))
        assertEquals(LocalDate.of(2011, 4, 30), LocalDate.of(2011, 1, 31).plusMonths(3))
        assertEquals(LocalDate.of(2011, 2, 28), LocalDate.of(2011, 3, 31).minusMonths(1))
        assertEquals(LocalDate.of(2010, 12, 31), LocalDate.of(2011, 1, 31).minusMonths(1))
    }

    @Test
    fun testLocalDatePlusMinusAndCompare() {
        val date = LocalDate.of(2020, 6, 15)
        assertEquals(LocalDate.of(2020, 6, 16), date.plusDays(1))
        assertEquals(LocalDate.of(2020, 6, 14), date.minusDays(1))
        assertEquals(LocalDate.of(2021, 6, 15), date.plusYears(1))
        assertEquals(LocalDate.of(2019, 6, 15), date.minusYears(1))
        assertTrue(date.isBefore(date.plusDays(1)))
        assertTrue(date.plusDays(1).isAfter(date))
        assertTrue(date < date.plusDays(1))
        assertEquals(0, date.compareTo(LocalDate.of(2020, 6, 15)))
        assertEquals(date, LocalDate.parse("2020-06-15"))
        assertNotEquals(date, LocalDate.of(2020, 6, 16))
    }

    @Test
    fun testLocalDateAtTime() {
        val date = LocalDate.of(2020, 1, 2)
        val time = LocalTime.of(3, 4, 5)
        val ldt = date.atTime(time)
        assertEquals(date, ldt.toLocalDate())
        assertEquals(time, ldt.toLocalTime())
        assertEquals(LocalDateTime.of(2020, 1, 2, 8, 30), date.atTime(8, 30))
    }

    @Test
    fun testLocalTimeIsoAndDayFractions() {
        assertEquals(0, LocalTime.of(0, 0).toSecondOfDay())
        assertEquals(3661, LocalTime.of(1, 1, 1).toSecondOfDay())
        assertEquals(0L, LocalTime.MIDNIGHT.toNanoOfDay())
        assertEquals(12L * 3600 * 1_000_000_000L, LocalTime.NOON.toNanoOfDay())

        val t = LocalTime.of(10, 15, 30, 123_000_000)
        assertEquals(10, t.hour)
        assertEquals(15, t.minute)
        assertEquals(30, t.second)
        assertEquals(123_000_000, t.nano)
        assertEquals(LocalTime.of(11, 15, 30, 123_000_000), t.plusHours(1))
        assertEquals(LocalTime.of(10, 16, 30, 123_000_000), t.plusMinutes(1))
        assertEquals(LocalTime.of(10, 15, 31, 123_000_000), t.plusSeconds(1))
        assertEquals(LocalTime.of(0, 15), LocalTime.of(23, 15).plusHours(1))
        assertEquals("10:15", LocalTime.of(10, 15).toString())
        assertEquals("10:15:30", LocalTime.of(10, 15, 30).toString())
        assertEquals(t, LocalTime.parse("10:15:30.123"))
        assertEquals(LocalTime.of(10, 15), LocalTime.parse("10:15"))
    }

    @Test
    fun testLocalDateTimePlusAndAtZone() {
        val ldt = LocalDateTime.of(2011, 12, 3, 10, 15, 30)
        assertEquals(LocalDate.of(2011, 12, 3), ldt.toLocalDate())
        assertEquals(LocalTime.of(10, 15, 30), ldt.toLocalTime())
        assertEquals(LocalDateTime.of(2011, 12, 4, 10, 15, 30), ldt.plusDays(1))
        assertEquals(LocalDateTime.of(2011, 12, 3, 11, 15, 30), ldt.plusHours(1))
        assertEquals(LocalDateTime.of(2011, 12, 4, 1, 15, 30), ldt.plusHours(15))
        assertEquals(ldt, LocalDateTime.of(LocalDate.of(2011, 12, 3), LocalTime.of(10, 15, 30)))
        assertEquals(ldt, LocalDateTime.parse("2011-12-03T10:15:30"))

        val zoned = ldt.atZone(ZoneOffset.of("+08:00"))
        assertEquals(ldt, zoned.toLocalDateTime())
        assertEquals(ZoneOffset.of("+08:00"), zoned.offset)
    }

    @Test
    fun testZoneOffsetOfPlus0800() {
        val offset = ZoneOffset.of("+08:00")
        assertEquals(8 * 3600, offset.totalSeconds)
        assertEquals(8 * 3600, offset.getTotalSeconds())
        assertEquals("+08:00", offset.id)
        assertEquals("+08:00", offset.getId())
        assertEquals(offset, ZoneOffset.ofHours(8))
        assertEquals(offset, ZoneOffset.ofHoursMinutes(8, 0))
        assertEquals(ZoneOffset.UTC, ZoneOffset.of("Z"))
        assertEquals(0, ZoneOffset.UTC.totalSeconds)
        assertEquals("Z", ZoneOffset.UTC.id)
        assertEquals(ZoneOffset.UTC, ZoneOffset.of("+00:00"))
        assertEquals(-5 * 3600, ZoneOffset.of("-05:00").totalSeconds)
        assertFailsWith<DateTimeException> {
            ZoneOffset.ofHours(19)
        }
    }

    @Test
    fun testZoneIdOfCommonIds() {
        assertEquals("Z", ZoneId.of("Z").id)
        assertEquals("UTC", ZoneId.of("UTC").id)
        assertEquals("GMT", ZoneId.of("GMT").id)
        val plus8 = ZoneId.of("+08:00")
        assertEquals("+08:00", plus8.id)
        assertTrue(plus8 is ZoneOffset)
        assertEquals(8 * 3600, (plus8 as ZoneOffset).totalSeconds)

        val ids = ZoneId.getAvailableZoneIds()
        assertTrue(ids.contains("America/New_York"))
        assertTrue(ZoneId.systemDefault().id.isNotEmpty())
        assertEquals("America/New_York", ZoneId.of("America/New_York").id)
    }

    @Test
    fun testPeriodOf123() {
        val p = Period.of(1, 2, 3)
        assertEquals(1, p.years)
        assertEquals(1, p.getYears())
        assertEquals(2, p.months)
        assertEquals(2, p.getMonths())
        assertEquals(3, p.days)
        assertEquals(3, p.getDays())
        assertEquals("P1Y2M3D", p.toString())
        assertEquals(p, Period.ofYears(1).plus(Period.ofMonths(2)).plus(Period.ofDays(3)))
        assertEquals(Period.of(1, 2, 7), p.plus(Period.ofDays(4)))
        assertEquals(Period.of(0, 2, 3), p.minus(Period.ofYears(1)))
        assertEquals(Period.ofDays(5), Period.ofDays(5))
        assertEquals(Period.ZERO, Period.of(0, 0, 0))
        assertTrue(Period.ZERO.isZero())
        assertFalse(p.isZero())
    }

    @Test
    fun testOffsetDateTimeToInstant() {
        val ldt = LocalDateTime.of(1970, 1, 1, 8, 0, 0)
        val odt = OffsetDateTime.of(ldt, ZoneOffset.of("+08:00"))
        assertEquals(0L, odt.toEpochSecond())
        assertEquals(Instant.EPOCH, odt.toInstant())
        assertEquals(ldt, odt.toLocalDateTime())
        assertEquals(ZoneOffset.of("+08:00"), odt.offset)

        val now = OffsetDateTime.now(ZoneOffset.UTC)
        assertEquals(now.toLocalDateTime(), LocalDateTime.ofInstant(now.toInstant(), ZoneOffset.UTC))
    }

    @Test
    fun testZonedDateTimeOffsetBased() {
        val ldt = LocalDateTime.of(1970, 1, 1, 8, 0)
        val zone = ZoneId.of("+08:00")
        val zdt = ZonedDateTime.of(ldt, zone)
        assertEquals(Instant.EPOCH, zdt.toInstant())
        assertEquals(ldt, zdt.toLocalDateTime())
        assertEquals(0L, zdt.toEpochSecond())
        val odt = zdt.toOffsetDateTime()
        assertEquals(ldt, odt.toLocalDateTime())
        assertEquals(ZoneOffset.of("+08:00"), odt.offset)
        assertEquals(zdt, ZonedDateTime.ofInstant(Instant.EPOCH, zone))
    }

    @Test
    fun testParseInvalidThrowsDateTimeParseException() {
        assertFailsWith<DateTimeParseException> {
            LocalDate.parse("not-a-date")
        }
        assertFailsWith<DateTimeParseException> {
            LocalDate.parse("2024-02-30")
        }
        assertFailsWith<DateTimeParseException> {
            LocalTime.parse("25:00")
        }
        assertFailsWith<DateTimeParseException> {
            LocalDateTime.parse("2024-01-01 10:15:30")
        }
        val ex = assertFailsWith<DateTimeParseException> {
            LocalDate.parse("xxxx")
        }
        assertEquals("xxxx", ex.parsedString)
        assertEquals("xxxx", ex.getParsedString())
    }

    @Test
    fun testNowIsConsistentWithInstant() {
        val instant = Instant.now()
        val utc = ZoneOffset.UTC
        val fromInstant = LocalDateTime.ofInstant(instant, utc)
        val date = LocalDate.now(utc)
        val dayDelta = kotlin.math.abs(date.toEpochDay() - fromInstant.toLocalDate().toEpochDay())
        assertTrue(dayDelta <= 1)
        assertEquals(fromInstant.toLocalDate(), LocalDate.ofEpochDay(instant.epochSecond.floorDiv(86400)))
        assertTrue(LocalTime.now(utc).toSecondOfDay() in 0..86399)
    }

    @Test
    fun testOfInstantKnownUtcMidnight() {
        val instant = Instant.ofEpochMilli(1_609_459_200_000L)
        val ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC)
        assertEquals(LocalDate.of(2021, 1, 1), ldt.toLocalDate())
        assertEquals(LocalTime.MIDNIGHT, ldt.toLocalTime())
        assertEquals(10957L, LocalDate.of(2000, 1, 1).toEpochDay())
        assertEquals(LocalDate.of(2000, 1, 1), LocalDate.ofEpochDay(10957))
    }

    @Test
    fun testUsDstSpringForwardGapAndFallBackOverlap() {
        val ny = ZoneId.of("America/New_York")

        // 2024 US Spring Forward: 2024-03-10 02:00 EST (-05:00) -> 03:00 EDT (-04:00)
        // Local time 02:30 is in the gap and must be pushed forward by 1 hour to 03:30-04:00
        val inGap = ZonedDateTime.of(2024, 3, 10, 2, 30, 0, 0, ny)
        assertEquals(LocalDateTime.of(2024, 3, 10, 3, 30), inGap.toLocalDateTime())
        assertEquals(ZoneOffset.ofHours(-4), inGap.offset)

        // Before gap at 01:30-05:00: plusHours(1) crosses into EDT 03:30-04:00, plusDays(1) stays at 01:30-04:00
        val beforeGap = ZonedDateTime.of(2024, 3, 10, 1, 30, 0, 0, ny)
        assertEquals(ZoneOffset.ofHours(-5), beforeGap.offset)
        val plusOneHour = beforeGap.plusHours(1)
        assertEquals(LocalDateTime.of(2024, 3, 10, 3, 30), plusOneHour.toLocalDateTime())
        assertEquals(ZoneOffset.ofHours(-4), plusOneHour.offset)
        val plusOneDay = beforeGap.plusDays(1)
        assertEquals(LocalDateTime.of(2024, 3, 11, 1, 30), plusOneDay.toLocalDateTime())
        assertEquals(ZoneOffset.ofHours(-4), plusOneDay.offset)

        // 2024 US Fall Back: 2024-11-03 02:00 EDT (-04:00) -> 01:00 EST (-05:00)
        // Local time 01:30 is in the overlap and defaults to earlier offset (-04:00)
        val inOverlap = ZonedDateTime.of(2024, 11, 3, 1, 30, 0, 0, ny)
        assertEquals(LocalDateTime.of(2024, 11, 3, 1, 30), inOverlap.toLocalDateTime())
        assertEquals(ZoneOffset.ofHours(-4), inOverlap.offset)

        val later = inOverlap.withLaterOffsetAtOverlap()
        assertEquals(LocalDateTime.of(2024, 11, 3, 1, 30), later.toLocalDateTime())
        assertEquals(ZoneOffset.ofHours(-5), later.offset)
        assertEquals(3600L, later.toEpochSecond() - inOverlap.toEpochSecond())

        val backToEarlier = later.withEarlierOffsetAtOverlap()
        assertEquals(ZoneOffset.ofHours(-4), backToEarlier.offset)

        // Parsing with explicit offset in overlap preserves the offset
        val parsedEarly = ZonedDateTime.parse("2024-11-03T01:30:00-04:00[America/New_York]")
        val parsedLate = ZonedDateTime.parse("2024-11-03T01:30:00-05:00[America/New_York]")
        assertEquals(ZoneOffset.ofHours(-4), parsedEarly.offset)
        assertEquals(ZoneOffset.ofHours(-5), parsedLate.offset)
    }

    @Test
    fun testEuDstSpringForwardAndFallBack() {
        val london = ZoneId.of("Europe/London")
        // 2024 EU Spring Forward: last Sunday of March (2024-03-31) at 01:00 UTC
        val londonGap = ZonedDateTime.of(2024, 3, 31, 1, 30, 0, 0, london)
        assertEquals(LocalDateTime.of(2024, 3, 31, 2, 30), londonGap.toLocalDateTime())
        assertEquals(ZoneOffset.ofHours(1), londonGap.offset)

        // 2024 EU Fall Back: last Sunday of October (2024-10-27) at 01:00 UTC (02:00 BST -> 01:00 GMT)
        val londonOverlap = ZonedDateTime.of(2024, 10, 27, 1, 30, 0, 0, london)
        assertEquals(ZoneOffset.ofHours(1), londonOverlap.offset)
        assertEquals(ZoneOffset.UTC, londonOverlap.withLaterOffsetAtOverlap().offset)

        val paris = ZoneId.of("Europe/Paris")
        // Paris Spring Forward: 02:00 CET (+01:00) -> 03:00 CEST (+02:00)
        val parisGap = ZonedDateTime.of(2024, 3, 31, 2, 30, 0, 0, paris)
        assertEquals(LocalDateTime.of(2024, 3, 31, 3, 30), parisGap.toLocalDateTime())
        assertEquals(ZoneOffset.ofHours(2), parisGap.offset)

        // Paris Fall Back: 03:00 CEST (+02:00) -> 02:00 CET (+01:00)
        val parisOverlap = ZonedDateTime.of(2024, 10, 27, 2, 30, 0, 0, paris)
        assertEquals(ZoneOffset.ofHours(2), parisOverlap.offset)
        assertEquals(ZoneOffset.ofHours(1), parisOverlap.withLaterOffsetAtOverlap().offset)
    }

    @Test
    fun testTimeZoneDstParity() {
        val tz = java.util.TimeZone.getTimeZone("America/New_York")
        assertTrue(tz.useDaylightTime())
        val winterEpochMs = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
        val summerEpochMs = ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, ZoneOffset.UTC).toInstant().toEpochMilli()
        assertEquals(-5 * 3600 * 1000, tz.getOffset(winterEpochMs))
        assertEquals(-4 * 3600 * 1000, tz.getOffset(summerEpochMs))
        assertFalse(tz.inDaylightTime(java.util.Date(winterEpochMs)))
        assertTrue(tz.inDaylightTime(java.util.Date(summerEpochMs)))
    }

    @Test
    fun testPeriodExtremesAndOverflow() {
        val maxYears = Period.ofYears(Int.MAX_VALUE)
        assertEquals(Int.MAX_VALUE, maxYears.years)
        assertFailsWith<ArithmeticException> {
            maxYears.plusYears(1)
        }
        assertFailsWith<ArithmeticException> {
            Period.ofYears(Int.MIN_VALUE).negated()
        }
        assertFailsWith<ArithmeticException> {
            Period.ofYears(1_000_000_000).multipliedBy(3)
        }
        assertEquals(Period.of(0, 0, 14), Period.parse("P2W"))
        assertEquals(Period.of(-1, -2, -3), Period.parse("-P1Y2M3D"))
        assertFailsWith<DateTimeParseException> {
            Period.parse("P1D2Y")
        }
        assertFailsWith<DateTimeParseException> {
            Period.parse("P999999999999Y")
        }
    }

    @Test
    fun testSouthernHemisphereDstTransitions() {
        val sydney = ZoneId.of("Australia/Sydney")
        // January is Southern Hemisphere summer (AEDT +11:00); July is winter (AEST +10:00)
        val sydneySummer = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, sydney)
        val sydneyWinter = ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, sydney)
        assertEquals(ZoneOffset.ofHours(11), sydneySummer.offset)
        assertEquals(ZoneOffset.ofHours(10), sydneyWinter.offset)

        // 2024 Sydney Spring Forward: first Sunday of October (2024-10-06) at 02:00 AEST (+10:00) -> 03:00 AEDT (+11:00)
        val sydneyGap = ZonedDateTime.of(2024, 10, 6, 2, 30, 0, 0, sydney)
        assertEquals(LocalDateTime.of(2024, 10, 6, 3, 30), sydneyGap.toLocalDateTime())
        assertEquals(ZoneOffset.ofHours(11), sydneyGap.offset)

        // 2024 Sydney Fall Back: first Sunday of April (2024-04-07) at 03:00 AEDT (+11:00) -> 02:00 AEST (+10:00)
        val sydneyOverlap = ZonedDateTime.of(2024, 4, 7, 2, 30, 0, 0, sydney)
        assertEquals(ZoneOffset.ofHours(11), sydneyOverlap.offset)
        assertEquals(ZoneOffset.ofHours(10), sydneyOverlap.withLaterOffsetAtOverlap().offset)

        // TimeZone parity for Southern Hemisphere
        val tzSydney = java.util.TimeZone.getTimeZone("Australia/Sydney")
        assertTrue(tzSydney.useDaylightTime())
        assertTrue(tzSydney.inDaylightTime(java.util.Date(sydneySummer.toInstant().toEpochMilli())))
        assertFalse(tzSydney.inDaylightTime(java.util.Date(sydneyWinter.toInstant().toEpochMilli())))

        val auckland = ZoneId.of("Pacific/Auckland")
        assertEquals(ZoneOffset.ofHours(13), ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, auckland).offset)
        assertEquals(ZoneOffset.ofHours(12), ZonedDateTime.of(2024, 7, 15, 12, 0, 0, 0, auckland).offset)
    }
}

