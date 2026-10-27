package net.bennyhuo.jako.stdlib.test

import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.TimeZone

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
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
}
