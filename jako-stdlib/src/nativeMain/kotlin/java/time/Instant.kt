package java.time

import java.io.Serializable
import java.lang.System
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAccessor

actual class Instant private constructor(
    actual val epochSecond: Long,
    actual val nano: Int
) : Serializable, Comparable<Instant>, TemporalAccessor {

    actual fun getEpochSecond(): Long = epochSecond
    actual fun getNano(): Int = nano

    actual fun toEpochMilli(): Long {
        if (epochSecond >= 0 || nano == 0) {
            val millis = epochSecond * 1000L
            return millis + (nano / 1000000L)
        } else {
            val millis = (epochSecond + 1) * 1000L
            return millis + (nano / 1000000L) - 1000L
        }
    }

    actual fun plusSeconds(secondsToAdd: Long): Instant {
        if (secondsToAdd == 0L) return this
        return ofEpochSecond(epochSecond + secondsToAdd, nano.toLong())
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
        val epochSec = epochSecond + secondsToAdd
        return ofEpochSecond(epochSec, nano + nanosToAdd)
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

    actual fun isAfter(otherInstant: Instant): Boolean = compareTo(otherInstant) > 0

    actual fun isBefore(otherInstant: Instant): Boolean = compareTo(otherInstant) < 0

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

    override fun toString(): String {
        return "Instant(epochSecond=$epochSecond, nano=$nano)"
    }

    actual companion object {
        actual val EPOCH: Instant = Instant(0L, 0)
        actual val MIN: Instant = ofEpochSecond(-31557014167219200L, 0)
        actual val MAX: Instant = ofEpochSecond(31556889864403199L, 999999999)

        actual fun now(): Instant = ofEpochMilli(System.currentTimeMillis())

        actual fun ofEpochMilli(epochMilli: Long): Instant {
            var secs = epochMilli / 1000L
            var mos = (epochMilli % 1000L).toInt()
            if (mos < 0) {
                mos += 1000
                secs--
            }
            return Instant(secs, mos * 1000000)
        }

        actual fun ofEpochSecond(epochSecond: Long): Instant = ofEpochSecond(epochSecond, 0L)

        actual fun ofEpochSecond(epochSecond: Long, nanoAdjustment: Long): Instant {
            var secs = epochSecond + nanoAdjustment / 1000000000L
            var nos = (nanoAdjustment % 1000000000L).toInt()
            if (nos < 0) {
                nos += 1000000000
                secs--
            }
            return Instant(secs, nos)
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
