package jako.test.time

import java.time.DateTimeException
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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

    @Test
    fun testDurationExtremesAndOverflows() {
        val maxDur = Duration.ofSeconds(Long.MAX_VALUE, 999_999_999L)
        assertEquals(Long.MAX_VALUE, maxDur.seconds)
        assertEquals(999_999_999, maxDur.nano)

        val minDur = Duration.ofSeconds(Long.MIN_VALUE, 0)
        assertEquals(Long.MIN_VALUE, minDur.seconds)
        assertEquals(0, minDur.nano)

        // Exact boundary for Long.MIN_VALUE millis and nanos
        val minMillisDur = Duration.ofMillis(Long.MIN_VALUE)
        assertEquals(Long.MIN_VALUE, minMillisDur.toMillis())
        val minNanosDur = Duration.ofNanos(Long.MIN_VALUE)
        assertEquals(Long.MIN_VALUE, minNanosDur.toNanos())

        // Large duration arithmetic exceeding 292 years (Long.MAX_VALUE nanos)
        val hundredThousandDays = Duration.ofDays(100_000)
        val doubled = hundredThousandDays.multipliedBy(2)
        assertEquals(Duration.ofDays(200_000), doubled)
        assertEquals(hundredThousandDays, doubled.dividedBy(2))

        // Overflows
        assertFailsWith<ArithmeticException> {
            maxDur.plusNanos(1)
        }
        assertFailsWith<ArithmeticException> {
            maxDur.plusSeconds(1)
        }
        assertFailsWith<ArithmeticException> {
            minDur.negated()
        }
        assertFailsWith<ArithmeticException> {
            Duration.ofDays(Long.MAX_VALUE)
        }
        assertFailsWith<ArithmeticException> {
            maxDur.toMillis()
        }
        assertEquals(8_640_000_000_000_000_000L, hundredThousandDays.toNanos())
        assertFailsWith<ArithmeticException> {
            Duration.ofDays(200_000).toNanos()
        }
        assertFailsWith<ArithmeticException> {
            hundredThousandDays.dividedBy(0)
        }

        // ISO-8601 Duration parsing edge cases
        assertEquals(Duration.ofSeconds(-65, -500_000_000), Duration.parse("-PT1M5.5S"))
        assertFailsWith<DateTimeParseException> {
            Duration.parse("PT1M2H")
        }
        assertFailsWith<DateTimeParseException> {
            Duration.parse("PT")
        }
    }

    @Test
    fun testInstantExtremesAndOverflows() {
        assertEquals(-31557014167219200L, Instant.MIN.epochSecond)
        assertEquals(0, Instant.MIN.nano)
        assertEquals(31556889864403199L, Instant.MAX.epochSecond)
        assertEquals(999_999_999, Instant.MAX.nano)

        assertFailsWith<DateTimeException> {
            Instant.MAX.plusSeconds(1)
        }
        assertFailsWith<DateTimeException> {
            Instant.MAX.plusNanos(1)
        }
        assertFailsWith<DateTimeException> {
            Instant.MIN.minusSeconds(1)
        }
        assertFailsWith<DateTimeException> {
            Instant.MIN.minusNanos(1)
        }
        assertFailsWith<DateTimeException> {
            Instant.ofEpochSecond(Long.MAX_VALUE)
        }
        assertFailsWith<ArithmeticException> {
            Instant.MAX.toEpochMilli()
        }
    }
}
