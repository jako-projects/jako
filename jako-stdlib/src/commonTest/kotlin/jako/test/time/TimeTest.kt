package jako.test.time

import java.time.Duration
import java.time.Instant
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class TimeTest {

    @Test
    fun testDurationCreationAndCalculations() {
        val d1 = Duration.ofMillis(2500)
        assertEquals(2L, d1.getSeconds())
        assertEquals(500_000_000, d1.getNano())
        assertEquals(2500L, d1.toMillis())
        assertEquals(2_500_000_000L, d1.toNanos())

        val d2 = Duration.ofSeconds(10)
        val sum = d1.plus(d2)
        assertEquals(12500L, sum.toMillis())

        val diff = d2.minus(d1)
        assertEquals(7500L, diff.toMillis())

        assertFalse(d1.isZero())
        assertTrue(Duration.ZERO.isZero())
    }

    @Test
    fun testInstantTimestamps() {
        val instant = Instant.ofEpochMilli(1600000000000L)
        assertEquals(1600000000L, instant.getEpochSecond())
        assertEquals(0, instant.getNano())
        assertEquals(1600000000000L, instant.toEpochMilli())

        val plusSec = instant.plusSeconds(30)
        assertEquals(1600000030000L, plusSec.toEpochMilli())
        assertTrue(plusSec.isAfter(instant))
        assertTrue(instant.isBefore(plusSec))

        val minusSec = instant.minusSeconds(10)
        assertEquals(1599999990000L, minusSec.toEpochMilli())
    }

    @Test
    fun testDateToInstantInteroperability() {
        val date = Date(1700000000000L)
        val instant = date.toInstant()
        assertEquals(1700000000000L, instant.toEpochMilli())

        val fromInstant = Date.from(instant)
        assertEquals(date.getTime(), fromInstant.getTime())
    }
}
