package java.time

import java.io.Serializable
import java.time.temporal.TemporalAmount

actual class Duration private constructor(
    actual val seconds: Long,
    actual val nano: Int
) : TemporalAmount, Serializable, Comparable<Duration> {

    actual fun getSeconds(): Long = seconds
    actual fun getNano(): Int = nano

    actual fun toMillis(): Long = seconds * 1000L + nano / 1000000L
    actual fun toNanos(): Long = seconds * 1000000000L + nano

    actual fun plus(duration: Duration): Duration {
        return ofSeconds(seconds + duration.seconds, (nano + duration.nano).toLong())
    }

    actual fun minus(duration: Duration): Duration {
        return ofSeconds(seconds - duration.seconds, (nano - duration.nano).toLong())
    }

    actual fun isZero(): Boolean = (seconds or nano.toLong()) == 0L

    actual fun isNegative(): Boolean = seconds < 0

    actual override fun compareTo(other: Duration): Int {
        val cmp = seconds.compareTo(other.seconds)
        if (cmp != 0) return cmp
        return nano.compareTo(other.nano)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Duration) return false
        return this.seconds == other.seconds && this.nano == other.nano
    }

    override fun hashCode(): Int {
        return (seconds xor (seconds ushr 32)).toInt() + (51 * nano)
    }

    override fun toString(): String {
        if (this == ZERO) return "PT0S"
        val sb = StringBuilder("PT")
        if (seconds < 0 && nano > 0) {
            if (seconds == -1L) sb.append("-0") else sb.append(seconds + 1)
        } else {
            sb.append(seconds)
        }
        if (nano > 0) {
            val pos = sb.length
            if (seconds < 0) {
                sb.append(2 * 1000000000L - nano)
            } else {
                sb.append(nano + 1000000000L)
            }
            while (sb[sb.length - 1] == '0') {
                sb.setLength(sb.length - 1)
            }
            sb[pos] = '.'
        }
        sb.append('S')
        return sb.toString()
    }

    actual companion object {
        actual val ZERO: Duration = Duration(0, 0)

        actual fun ofMillis(millis: Long): Duration {
            var secs = millis / 1000L
            var mos = (millis % 1000L).toInt()
            if (mos < 0) {
                mos += 1000
                secs--
            }
            return Duration(secs, mos * 1000000)
        }

        actual fun ofSeconds(seconds: Long): Duration {
            return ofSeconds(seconds, 0L)
        }

        actual fun ofSeconds(seconds: Long, nanoAdjustment: Long): Duration {
            var secs = seconds + nanoAdjustment / 1000000000L
            var nos = (nanoAdjustment % 1000000000L).toInt()
            if (nos < 0) {
                nos += 1000000000
                secs--
            }
            return Duration(secs, nos)
        }

        actual fun ofNanos(nanos: Long): Duration {
            var secs = nanos / 1000000000L
            var nos = (nanos % 1000000000L).toInt()
            if (nos < 0) {
                nos += 1000000000
                secs--
            }
            return Duration(secs, nos)
        }

        actual fun ofMinutes(minutes: Long): Duration = ofSeconds(minutes * 60L)

        actual fun ofHours(hours: Long): Duration = ofSeconds(hours * 3600L)

        actual fun ofDays(days: Long): Duration = ofSeconds(days * 86400L)
    }
}
