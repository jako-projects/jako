package java.time

import java.io.Serializable
import java.lang.System
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit
import java.time.temporal.UnsupportedTemporalTypeException

actual class Instant private constructor(
    actual val epochSecond: Long,
    actual val nano: Int
) : Temporal, TemporalAdjuster, Serializable, Comparable<Instant> {

    actual fun getEpochSecond(): Long = epochSecond
    actual fun getNano(): Int = nano

    actual fun toEpochMilli(): Long {
        if (epochSecond < 0 && nano > 0) {
            val millis = multiplyExact(epochSecond + 1, 1000L)
            val adjustment = (nano / 1_000_000L) - 1000L
            return addExact(millis, adjustment)
        } else {
            val millis = multiplyExact(epochSecond, 1000L)
            return addExact(millis, (nano / 1_000_000L))
        }
    }

    actual fun truncatedTo(unit: TemporalUnit): Instant {
        if (unit == ChronoUnit.NANOS) return this
        val unitDur = unit.duration
        if (unitDur.seconds > SECONDS_PER_DAY) {
            throw UnsupportedTemporalTypeException("Unit is too large to be used for truncation")
        }
        val dur = unitDur.toNanos()
        if ((NANOS_PER_DAY % dur) != 0L) {
            throw UnsupportedTemporalTypeException("Unit must divide into a standard day without remainder")
        }
        val nod = (epochSecond % SECONDS_PER_DAY) * NANOS_PER_SECOND + nano
        val result = nod.floorDiv(dur) * dur
        return plusNanos(result - nod)
    }

    actual fun plus(amountToAdd: TemporalAmount): Instant {
        if (amountToAdd is Duration) {
            return plus(amountToAdd.seconds, amountToAdd.nano.toLong())
        }
        if (amountToAdd is Period) {
            if (amountToAdd.years != 0 || amountToAdd.months != 0) {
                throw UnsupportedTemporalTypeException("Instant does not support Period with years or months")
            }
            return plusSeconds(multiplyExact(amountToAdd.days.toLong(), SECONDS_PER_DAY))
        }
        throw DateTimeException("Unsupported TemporalAmount for Instant: $amountToAdd")
    }

    actual fun plus(amountToAdd: Long, unit: TemporalUnit): Instant {
        if (unit is ChronoUnit) {
            return when (unit) {
                ChronoUnit.NANOS -> plusNanos(amountToAdd)
                ChronoUnit.MICROS -> plus(amountToAdd / 1_000_000L, (amountToAdd % 1_000_000L) * 1000L)
                ChronoUnit.MILLIS -> plusMillis(amountToAdd)
                ChronoUnit.SECONDS -> plusSeconds(amountToAdd)
                ChronoUnit.MINUTES -> plusSeconds(multiplyExact(amountToAdd, 60L))
                ChronoUnit.HOURS -> plusSeconds(multiplyExact(amountToAdd, 3600L))
                ChronoUnit.HALF_DAYS -> plusSeconds(multiplyExact(amountToAdd, 43200L))
                ChronoUnit.DAYS -> plusSeconds(multiplyExact(amountToAdd, SECONDS_PER_DAY))
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.addTo(this, amountToAdd)
    }

    actual fun plusSeconds(secondsToAdd: Long): Instant {
        if (secondsToAdd == 0L) return this
        return plus(secondsToAdd, 0L)
    }

    actual fun plusMillis(millisToAdd: Long): Instant {
        if (millisToAdd == 0L) return this
        return plus(millisToAdd / 1000L, (millisToAdd % 1000L) * 1000000L)
    }

    actual fun plusNanos(nanosToAdd: Long): Instant {
        if (nanosToAdd == 0L) return this
        return plus(0L, nanosToAdd)
    }

    private fun plus(secondsToAdd: Long, nanosToAdd: Long): Instant {
        if ((secondsToAdd or nanosToAdd) == 0L) return this
        var epochSec = addExact(epochSecond, secondsToAdd)
        epochSec = addExact(epochSec, nanosToAdd / NANOS_PER_SECOND)
        val newNanos = nano + (nanosToAdd % NANOS_PER_SECOND)
        return ofEpochSecond(epochSec, newNanos)
    }

    actual fun minus(amountToSubtract: TemporalAmount): Instant {
        if (amountToSubtract is Duration) {
            val secsToSubtract = amountToSubtract.seconds
            val nanosToSubtract = amountToSubtract.nano
            if (secsToSubtract == Long.MIN_VALUE) {
                return plus(Long.MAX_VALUE, -nanosToSubtract.toLong()).plus(1L, 0L)
            }
            return plus(-secsToSubtract, -nanosToSubtract.toLong())
        }
        if (amountToSubtract is Period) {
            if (amountToSubtract.years != 0 || amountToSubtract.months != 0) {
                throw UnsupportedTemporalTypeException("Instant does not support Period with years or months")
            }
            return minusSeconds(multiplyExact(amountToSubtract.days.toLong(), SECONDS_PER_DAY))
        }
        throw DateTimeException("Unsupported TemporalAmount for Instant: $amountToSubtract")
    }

    actual fun minus(amountToSubtract: Long, unit: TemporalUnit): Instant {
        return if (amountToSubtract == Long.MIN_VALUE) {
            plus(Long.MAX_VALUE, unit).plus(1, unit)
        } else {
            plus(-amountToSubtract, unit)
        }
    }

    actual fun minusSeconds(secondsToSubtract: Long): Instant {
        if (secondsToSubtract == Long.MIN_VALUE) {
            return plusSeconds(Long.MAX_VALUE).plusSeconds(1)
        }
        return plusSeconds(-secondsToSubtract)
    }

    actual fun minusMillis(millisToSubtract: Long): Instant {
        if (millisToSubtract == Long.MIN_VALUE) {
            return plusMillis(Long.MAX_VALUE).plusMillis(1)
        }
        return plusMillis(-millisToSubtract)
    }

    actual fun minusNanos(nanosToSubtract: Long): Instant {
        if (nanosToSubtract == Long.MIN_VALUE) {
            return plusNanos(Long.MAX_VALUE).plusNanos(1)
        }
        return plusNanos(-nanosToSubtract)
    }

    actual fun until(endExclusive: Temporal, unit: TemporalUnit): Long {
        val end = from(endExclusive)
        if (unit is ChronoUnit) {
            val secsDiff = addExact(end.epochSecond, -epochSecond)
            val nanosDiff = (end.nano - nano).toLong()
            return when (unit) {
                ChronoUnit.NANOS -> addExact(multiplyExact(secsDiff, NANOS_PER_SECOND), nanosDiff)
                ChronoUnit.MICROS -> addExact(multiplyExact(secsDiff, 1_000_000L), nanosDiff / 1000L)
                ChronoUnit.MILLIS -> addExact(end.toEpochMilli(), -toEpochMilli())
                ChronoUnit.SECONDS -> secsUntil(end)
                ChronoUnit.MINUTES -> secsUntil(end) / 60L
                ChronoUnit.HOURS -> secsUntil(end) / 3600L
                ChronoUnit.HALF_DAYS -> secsUntil(end) / 43200L
                ChronoUnit.DAYS -> secsUntil(end) / SECONDS_PER_DAY
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $unit")
            }
        }
        return unit.between(this, end)
    }

    private fun secsUntil(end: Instant): Long {
        var secsDiff = addExact(end.epochSecond, -epochSecond)
        val nanosDiff = end.nano - nano
        if (secsDiff > 0 && nanosDiff < 0) {
            secsDiff--
        } else if (secsDiff < 0 && nanosDiff > 0) {
            secsDiff++
        }
        return secsDiff
    }

    actual fun atOffset(offset: ZoneOffset): OffsetDateTime = OffsetDateTime.ofInstant(this, offset)

    actual fun atZone(zone: ZoneId): ZonedDateTime = ZonedDateTime.ofInstant(this, zone)

    actual fun isAfter(otherInstant: Instant): Boolean = compareTo(otherInstant) > 0

    actual fun isBefore(otherInstant: Instant): Boolean = compareTo(otherInstant) < 0

    actual override fun adjustInto(temporal: Temporal): Temporal {
        if (temporal is Instant) return this
        throw DateTimeException("Unable to adjust temporal with Instant: $temporal")
    }

    actual override fun compareTo(other: Instant): Int {
        val cmp = epochSecond.compareTo(other.epochSecond)
        if (cmp != 0) return cmp
        return nano.compareTo(other.nano)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Instant) return false
        return this.epochSecond == other.epochSecond && this.nano == other.nano
    }

    override fun hashCode(): Int {
        return (epochSecond xor (epochSecond ushr 32)).toInt() + (51 * nano)
    }

    override fun toString(): String = DateTimeFormatter.ISO_INSTANT.format(this)

    actual companion object {
        private const val MIN_SECOND = -31557014167219200L
        private const val MAX_SECOND = 31556889864403199L

        actual val EPOCH: Instant = Instant(0L, 0)
        actual val MIN: Instant = ofEpochSecond(MIN_SECOND, 0)
        actual val MAX: Instant = ofEpochSecond(MAX_SECOND, 999999999)

        actual fun now(): Instant = ofEpochMilli(System.currentTimeMillis())

        actual fun now(clock: Clock): Instant = clock.instant()

        actual fun ofEpochMilli(epochMilli: Long): Instant {
            val secs = epochMilli.floorDiv(1000L)
            val mos = epochMilli.mod(1000L).toInt()
            return create(secs, mos * 1000000)
        }

        actual fun ofEpochSecond(epochSecond: Long): Instant = create(epochSecond, 0)

        actual fun ofEpochSecond(epochSecond: Long, nanoAdjustment: Long): Instant {
            val secs = addExact(epochSecond, nanoAdjustment.floorDiv(1000000000L))
            val nos = nanoAdjustment.mod(1000000000L).toInt()
            return create(secs, nos)
        }

        private fun create(seconds: Long, nanoOfSecond: Int): Instant {
            if ((seconds or nanoOfSecond.toLong()) == 0L) {
                return EPOCH
            }
            if (seconds < MIN_SECOND || seconds > MAX_SECOND) {
                throw DateTimeException("Instant exceeds minimum or maximum instant")
            }
            return Instant(seconds, nanoOfSecond)
        }

        actual fun parse(text: CharSequence): Instant {
            // [Nullability Decision] text is non-null, matching JDK 8 Instant.parse
            return from(DateTimeFormatter.ISO_INSTANT.parse(text))
        }

        actual fun from(temporal: TemporalAccessor): Instant {
            if (temporal is Instant) return temporal
            if (temporal is OffsetDateTime) return temporal.toInstant()
            if (temporal is ZonedDateTime) return temporal.toInstant()
            throw DateTimeException("Unable to obtain Instant from TemporalAccessor: $temporal")
        }
    }
}
