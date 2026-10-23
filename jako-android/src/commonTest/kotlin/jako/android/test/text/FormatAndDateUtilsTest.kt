package jako.android.test.text

import android.text.format.DateUtils
import android.text.format.Formatter
import com.bennyhuo.jako.JakoApplication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormatAndDateUtilsTest {

    @Test
    fun testFormatterFileSize() {
        val ctx = JakoApplication.applicationContext
        assertEquals("0 B", Formatter.formatFileSize(null, 0L))
        assertEquals("512 B", Formatter.formatFileSize(ctx, 512L))
        assertEquals("1.50 kB", Formatter.formatFileSize(ctx, 1_500L))
        assertEquals("1.5 kB", Formatter.formatShortFileSize(ctx, 1_500L))
        assertEquals("25.6 MB", Formatter.formatFileSize(null, 25_600_000L))
        assertEquals("26 MB", Formatter.formatShortFileSize(null, 25_600_000L))
        assertEquals("-2.00 GB", Formatter.formatFileSize(null, -2_000_000_000L))
        assertEquals("3.50 TB", Formatter.formatFileSize(null, 3_500_000_000_000L))
    }

    @Test
    fun testDateUtilsElapsedTimeAndIsToday() {
        assertEquals(1000L, DateUtils.SECOND_IN_MILLIS)
        assertEquals(60_000L, DateUtils.MINUTE_IN_MILLIS)
        assertEquals(3_600_000L, DateUtils.HOUR_IN_MILLIS)
        assertEquals(86_400_000L, DateUtils.DAY_IN_MILLIS)
        assertEquals(604_800_000L, DateUtils.WEEK_IN_MILLIS)
        assertEquals(31_449_600_000L, DateUtils.YEAR_IN_MILLIS)

        assertEquals("00:00", DateUtils.formatElapsedTime(-10L))
        assertEquals("00:00", DateUtils.formatElapsedTime(0L))
        assertEquals("00:09", DateUtils.formatElapsedTime(9L))
        assertEquals("03:45", DateUtils.formatElapsedTime(225L))
        assertEquals("1:02:03", DateUtils.formatElapsedTime(3723L))

        val recycle = StringBuilder("initial")
        assertEquals("12:34", DateUtils.formatElapsedTime(recycle, 754L))
        assertEquals("12:34", recycle.toString())

        val now = System.currentTimeMillis()
        assertTrue(DateUtils.isToday(now))
        assertFalse(DateUtils.isToday(now - 2 * DateUtils.DAY_IN_MILLIS))
        assertFalse(DateUtils.isToday(now + 2 * DateUtils.DAY_IN_MILLIS))
    }
}
