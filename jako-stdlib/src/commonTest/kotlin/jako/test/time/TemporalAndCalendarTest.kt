package jako.test.time

import java.time.*
import java.time.temporal.*
import kotlin.test.*

class TemporalAndCalendarTest {

    @Test
    fun testValueRangeAndChronoField() {
        val range = ValueRange.of(1L, 28L, 31L)
        assertFalse(range.isFixed())
        assertTrue(range.isIntValue())
        assertEquals(1L, range.getMinimum())
        assertEquals(28L, range.getSmallestMaximum())
        assertEquals(31L, range.getMaximum())
        assertTrue(range.isValidValue(15L))
        assertFalse(range.isValidValue(32L))
        assertEquals(15, range.checkValidIntValue(15L, ChronoField.DAY_OF_MONTH))
        assertFailsWith<DateTimeException> {
            range.checkValidValue(0L, ChronoField.DAY_OF_MONTH)
        }

        assertTrue(ChronoField.YEAR.isDateBased())
        assertFalse(ChronoField.YEAR.isTimeBased())
        assertTrue(ChronoField.HOUR_OF_DAY.isTimeBased())
        assertFalse(ChronoField.HOUR_OF_DAY.isDateBased())
        assertEquals(2026, ChronoField.YEAR.checkValidIntValue(2026L))
    }

    @Test
    fun testChronoUnit() {
        assertTrue(ChronoUnit.DAYS.isDateBased())
        assertFalse(ChronoUnit.DAYS.isTimeBased())
        assertTrue(ChronoUnit.HOURS.isTimeBased())
        assertFalse(ChronoUnit.HOURS.isDateBased())

        val d1 = LocalDate.of(2026, 3, 1)
        val d2 = LocalDate.of(2026, 3, 15)
        assertEquals(14L, ChronoUnit.DAYS.between(d1, d2))
        assertEquals(2L, ChronoUnit.WEEKS.between(d1, d2))
        assertEquals(d2, ChronoUnit.DAYS.addTo(d1, 14L))
    }

    @Test
    fun testDayOfWeekAndTemporalAdjusters() {
        val dow = DayOfWeek.of(1)
        assertEquals(DayOfWeek.MONDAY, dow)
        assertEquals(DayOfWeek.WEDNESDAY, dow.plus(2))
        assertEquals(DayOfWeek.SUNDAY, dow.minus(1))
        assertEquals(1, dow.getValue())

        val date = LocalDate.of(2026, 2, 18) // Wednesday
        assertEquals(DayOfWeek.WEDNESDAY, date.getDayOfWeek())
        assertEquals(LocalDate.of(2026, 2, 1), date.with(TemporalAdjusters.firstDayOfMonth()))
        assertEquals(LocalDate.of(2026, 2, 28), date.with(TemporalAdjusters.lastDayOfMonth()))
        assertEquals(LocalDate.of(2026, 3, 1), date.with(TemporalAdjusters.firstDayOfNextMonth()))
        assertEquals(LocalDate.of(2026, 1, 1), date.with(TemporalAdjusters.firstDayOfYear()))
        assertEquals(LocalDate.of(2026, 12, 31), date.with(TemporalAdjusters.lastDayOfYear()))
        assertEquals(LocalDate.of(2027, 1, 1), date.with(TemporalAdjusters.firstDayOfNextYear()))
        assertEquals(LocalDate.of(2026, 2, 20), date.with(TemporalAdjusters.next(DayOfWeek.FRIDAY)))
        assertEquals(LocalDate.of(2026, 2, 18), date.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY)))
        assertEquals(LocalDate.of(2026, 2, 16), date.with(TemporalAdjusters.previous(DayOfWeek.MONDAY)))
        assertEquals(LocalDate.of(2026, 2, 2), date.with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY)))
        assertEquals(LocalDate.of(2026, 2, 23), date.with(TemporalAdjusters.lastInMonth(DayOfWeek.MONDAY)))
    }

    @Test
    fun testYearYearMonthMonthDay() {
        val y2024 = Year.of(2024)
        val y2026 = Year.parse("2026")
        assertTrue(y2024.isLeap())
        assertFalse(y2026.isLeap())
        assertEquals(366, y2024.length())
        assertEquals(365, y2026.length())
        assertEquals(LocalDate.of(2024, 2, 29), y2024.atDay(60))
        assertEquals(YearMonth.of(2026, 5), y2026.atMonth(5))
        assertEquals(2L, y2024.until(y2026, ChronoUnit.YEARS))

        val ym = YearMonth.parse("2024-02")
        assertTrue(ym.isLeapYear())
        assertEquals(29, ym.lengthOfMonth())
        assertEquals(LocalDate.of(2024, 2, 29), ym.atEndOfMonth())
        assertEquals(YearMonth.of(2024, 5), ym.plusMonths(3))
        assertEquals(3L, ym.until(YearMonth.of(2024, 5), ChronoUnit.MONTHS))

        val md = MonthDay.parse("--02-29")
        assertEquals(Month.FEBRUARY, md.getMonth())
        assertEquals(29, md.getDayOfMonth())
        assertTrue(md.isValidYear(2024))
        assertFalse(md.isValidYear(2026))
        assertEquals(LocalDate.of(2024, 2, 29), md.atYear(2024))
        assertEquals(LocalDate.of(2026, 2, 28), md.atYear(2026))
    }

    @Test
    fun testOffsetTimeAndClock() {
        val ot = OffsetTime.parse("10:15:30+02:00")
        assertEquals(LocalTime.of(10, 15, 30), ot.toLocalTime())
        assertEquals(ZoneOffset.ofHours(2), ot.getOffset())
        val utcOt = ot.withOffsetSameInstant(ZoneOffset.UTC)
        assertEquals(LocalTime.of(8, 15, 30), utcOt.toLocalTime())
        assertTrue(ot.isEqual(utcOt))

        val fixedInstant = Instant.ofEpochSecond(1700000000L, 123_456_789L)
        val clock = Clock.fixed(fixedInstant, ZoneOffset.UTC)
        assertEquals(fixedInstant, clock.instant())
        assertEquals(fixedInstant.toEpochMilli(), clock.millis())

        val offsetClock = Clock.offset(clock, Duration.ofSeconds(60))
        assertEquals(fixedInstant.plusSeconds(60), offsetClock.instant())

        val tickSecClock = Clock.tickSeconds(ZoneOffset.UTC)
        assertEquals(0, tickSecClock.instant().getNano())
    }

    @Test
    fun testExpandedTemporalOperations() {
        val dt = LocalDateTime.of(2026, 4, 17, 14, 35, 45, 987_000_000)
        assertEquals(LocalDateTime.of(2026, 4, 17, 14, 0, 0, 0), dt.truncatedTo(ChronoUnit.HOURS))
        assertEquals(2026, dt.get(ChronoField.YEAR))
        assertEquals(4, dt.get(ChronoField.MONTH_OF_YEAR))
        assertEquals(17, dt.get(ChronoField.DAY_OF_MONTH))
        assertEquals(14, dt.get(ChronoField.HOUR_OF_DAY))

        val dt2 = dt.plus(2, ChronoUnit.DAYS).minus(3, ChronoUnit.HOURS)
        assertEquals(45L, dt.until(dt2, ChronoUnit.HOURS))

        val inst = Instant.ofEpochSecond(3661L, 500_000_000L)
        assertEquals(Instant.ofEpochSecond(3600L), inst.truncatedTo(ChronoUnit.HOURS))
        assertEquals(Instant.ofEpochSecond(3661L), inst.truncatedTo(ChronoUnit.SECONDS))

        val dur = Duration.ofHours(2).plusMinutes(30).dividedBy(3)
        assertEquals(50L, dur.toMinutes())

        val p = Period.ofWeeks(3).plusDays(4).normalized()
        assertEquals(25, p.getDays())
    }
}
