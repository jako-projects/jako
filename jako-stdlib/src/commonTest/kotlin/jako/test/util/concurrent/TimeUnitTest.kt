package jako.test.util.concurrent

import java.util.concurrent.*

import kotlin.test.*

class TimeUnitTest {

    @Test
    fun testConversions() {
        assertEquals(1_000_000L, TimeUnit.MILLISECONDS.toNanos(1L))
        assertEquals(1_000L, TimeUnit.SECONDS.toMillis(1L))
        assertEquals(60L, TimeUnit.MINUTES.toSeconds(1L))
        assertEquals(60L, TimeUnit.HOURS.toMinutes(1L))
        assertEquals(24L, TimeUnit.DAYS.toHours(1L))
    }

    @Test
    fun testConvert() {
        assertEquals(5000L, TimeUnit.MILLISECONDS.convert(5L, TimeUnit.SECONDS))
        assertEquals(2L, TimeUnit.MINUTES.convert(120L, TimeUnit.SECONDS))
    }
}
