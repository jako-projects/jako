package net.bennyhuo.jako.stdlib.test

import java.util.Calendar
import java.util.Date
import java.util.TimeZone

import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarTest {

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
        assert(date.time > 0L)
    }
}
