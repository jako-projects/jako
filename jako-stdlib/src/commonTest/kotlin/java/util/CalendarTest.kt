package net.bennyhuo.jako.stdlib.test

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class CalendarTest {

    @Test
    fun testDefaultCalendarInitializedToCurrentTime() {
        val before = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        val greg = GregorianCalendar()
        val after = System.currentTimeMillis()

        assertTrue(cal.timeInMillis in (before - 1000L)..(after + 1000L))
        assertTrue(greg.timeInMillis in (before - 1000L)..(after + 1000L))
        assertTrue(abs(cal.timeInMillis - greg.timeInMillis) < 2000L)

        val dateOnly = GregorianCalendar(2026, Calendar.OCTOBER, 22)
        assertEquals(2026, dateOnly.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, dateOnly.get(Calendar.MONTH))
        assertEquals(22, dateOnly.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, dateOnly.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, dateOnly.get(Calendar.MINUTE))
        assertEquals(0, dateOnly.get(Calendar.SECOND))
        assertEquals(0, dateOnly.get(Calendar.MILLISECOND))
    }

    @Test
    fun testCalendarFields() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.timeInMillis = 1700000000000L // 2023-11-14 22:13:20 UTC

        assertEquals(2023, cal.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, cal.get(Calendar.MONTH))
        assertEquals(14, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(22, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(13, cal.get(Calendar.MINUTE))
        assertEquals(20, cal.get(Calendar.SECOND))
        assertEquals(Calendar.TUESDAY, cal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun testCalendarSetAndGetTime() {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        cal.set(2026, Calendar.AUGUST, 11, 16, 40, 0)
        val date = cal.getTime()
        assertTrue(date.time > 0L)
    }

    @Test
    fun testUtcWallTimeEpoch() {
        val cal = utcCalendar()
        cal.set(2026, Calendar.AUGUST, 11, 16, 40, 0)
        cal.set(Calendar.MILLISECOND, 0)
        assertEquals(1786466400000L, cal.timeInMillis)
    }

    @Test
    fun testLeapDayFieldsAndHourOfDay() {
        val cal = utcCalendar()
        cal.set(2024, Calendar.FEBRUARY, 29, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)

        assertEquals(2024, cal.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, cal.get(Calendar.MONTH))
        assertEquals(29, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.THURSDAY, cal.get(Calendar.DAY_OF_WEEK))
        assertEquals(60, cal.get(Calendar.DAY_OF_YEAR))
        assertEquals(0, cal.get(Calendar.HOUR))
        assertEquals(0, cal.get(Calendar.AM_PM))
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.ZONE_OFFSET))
        assertEquals(0, cal.get(Calendar.DST_OFFSET))
        assertEquals(1709164800000L, cal.timeInMillis)

        cal.set(Calendar.HOUR_OF_DAY, 15)
        assertEquals(3, cal.get(Calendar.HOUR))
        assertEquals(1, cal.get(Calendar.AM_PM))
    }

    @Test
    fun testLenientFebruaryDayOverflow() {
        val cal = utcCalendar()
        assertTrue(cal.isLenient)

        cal.set(2023, Calendar.FEBRUARY, 29, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        assertEquals(2023, cal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH))
        assertEquals(1, cal.get(Calendar.DAY_OF_MONTH))

        cal.set(2023, Calendar.FEBRUARY, 30, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        assertEquals(2023, cal.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, cal.get(Calendar.MONTH))
        assertEquals(2, cal.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testAddPinsDayToMonthLength() {
        val leap = utcCalendar()
        leap.set(2024, Calendar.JANUARY, 31, 0, 0, 0)
        leap.set(Calendar.MILLISECOND, 0)
        leap.add(Calendar.MONTH, 1)
        assertEquals(2024, leap.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, leap.get(Calendar.MONTH))
        assertEquals(29, leap.get(Calendar.DAY_OF_MONTH))

        val common = utcCalendar()
        common.set(2023, Calendar.JANUARY, 31, 0, 0, 0)
        common.set(Calendar.MILLISECOND, 0)
        common.add(Calendar.MONTH, 1)
        assertEquals(2023, common.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, common.get(Calendar.MONTH))
        assertEquals(28, common.get(Calendar.DAY_OF_MONTH))

        val nextYear = utcCalendar()
        nextYear.set(2024, Calendar.FEBRUARY, 29, 0, 0, 0)
        nextYear.set(Calendar.MILLISECOND, 0)
        nextYear.add(Calendar.YEAR, 1)
        assertEquals(2025, nextYear.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, nextYear.get(Calendar.MONTH))
        assertEquals(28, nextYear.get(Calendar.DAY_OF_MONTH))

        val previousDay = utcCalendar()
        previousDay.set(2024, Calendar.MARCH, 1, 0, 0, 0)
        previousDay.set(Calendar.MILLISECOND, 0)
        previousDay.add(Calendar.DAY_OF_MONTH, -1)
        assertEquals(2024, previousDay.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, previousDay.get(Calendar.MONTH))
        assertEquals(29, previousDay.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testLenientMonthAndDayNormalization() {
        val monthOverflow = utcCalendar()
        monthOverflow.set(2024, Calendar.JANUARY, 15, 0, 0, 0)
        monthOverflow.set(Calendar.MILLISECOND, 0)
        monthOverflow.set(Calendar.MONTH, 13)
        assertEquals(2025, monthOverflow.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, monthOverflow.get(Calendar.MONTH))
        assertEquals(15, monthOverflow.get(Calendar.DAY_OF_MONTH))

        val dayUnderflow = utcCalendar()
        dayUnderflow.set(2024, Calendar.MARCH, 1, 0, 0, 0)
        dayUnderflow.set(Calendar.MILLISECOND, 0)
        dayUnderflow.set(Calendar.DAY_OF_MONTH, 0)
        assertEquals(2024, dayUnderflow.get(Calendar.YEAR))
        assertEquals(Calendar.FEBRUARY, dayUnderflow.get(Calendar.MONTH))
        assertEquals(29, dayUnderflow.get(Calendar.DAY_OF_MONTH))
    }

    @Test
    fun testMillisBeforeEpoch() {
        val cal = utcCalendar()
        cal.timeInMillis = -1L
        assertEquals(1969, cal.get(Calendar.YEAR))
        assertEquals(Calendar.DECEMBER, cal.get(Calendar.MONTH))
        assertEquals(31, cal.get(Calendar.DAY_OF_MONTH))
        assertEquals(23, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(59, cal.get(Calendar.MINUTE))
        assertEquals(59, cal.get(Calendar.SECOND))
        assertEquals(999, cal.get(Calendar.MILLISECOND))
        assertEquals(Calendar.WEDNESDAY, cal.get(Calendar.DAY_OF_WEEK))
    }

    @Test
    fun testNonLenientRejectsInvalidLeapDay() {
        val cal = utcCalendar()
        cal.isLenient = false
        cal.set(2023, Calendar.FEBRUARY, 29, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        assertFailsWith<IllegalArgumentException> {
            cal.getTime()
        }
    }

    @Test
    fun testCompareToByInstant() {
        val earlier = utcCalendar()
        earlier.timeInMillis = 0L
        val later = utcCalendar()
        later.timeInMillis = 1L
        assertEquals(-1, earlier.compareTo(later))
        assertEquals(0, earlier.compareTo(earlier))
    }

    @Test
    fun testNewYorkStandardAndDaylightFields() {
        val summer = newYorkCalendar()
        summer.set(2024, Calendar.JULY, 15, 12, 0, 0)
        summer.set(Calendar.MILLISECOND, 0)
        assertEquals(-18000000, summer.get(Calendar.ZONE_OFFSET))
        assertEquals(3600000, summer.get(Calendar.DST_OFFSET))
        assertEquals(1721059200000L, summer.timeInMillis)

        val winter = newYorkCalendar()
        winter.set(2024, Calendar.JANUARY, 15, 12, 0, 0)
        winter.set(Calendar.MILLISECOND, 0)
        assertEquals(0, winter.get(Calendar.DST_OFFSET))
        assertEquals(1705338000000L, winter.timeInMillis)
    }

    @Test
    fun testNewYorkGapAndOverlap() {
        val gap = newYorkCalendar()
        gap.set(2024, Calendar.MARCH, 10, 2, 30, 0)
        gap.set(Calendar.MILLISECOND, 0)
        assertEquals(2024, gap.get(Calendar.YEAR))
        assertEquals(Calendar.MARCH, gap.get(Calendar.MONTH))
        assertEquals(10, gap.get(Calendar.DAY_OF_MONTH))
        assertEquals(3, gap.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, gap.get(Calendar.MINUTE))
        assertEquals(3600000, gap.get(Calendar.DST_OFFSET))
        assertEquals(1710055800000L, gap.timeInMillis)

        val overlap = newYorkCalendar()
        overlap.set(2024, Calendar.NOVEMBER, 3, 1, 30, 0)
        overlap.set(Calendar.MILLISECOND, 0)
        assertEquals(2024, overlap.get(Calendar.YEAR))
        assertEquals(Calendar.NOVEMBER, overlap.get(Calendar.MONTH))
        assertEquals(3, overlap.get(Calendar.DAY_OF_MONTH))
        assertEquals(1, overlap.get(Calendar.HOUR_OF_DAY))
        assertEquals(30, overlap.get(Calendar.MINUTE))
        assertEquals(0, overlap.get(Calendar.DST_OFFSET))
        assertEquals(1730615400000L, overlap.timeInMillis)
    }

    private fun utcCalendar(): GregorianCalendar = GregorianCalendar(TimeZone.getTimeZone("UTC"))

    private fun newYorkCalendar(): GregorianCalendar = GregorianCalendar(TimeZone.getTimeZone("America/New_York"))
}
