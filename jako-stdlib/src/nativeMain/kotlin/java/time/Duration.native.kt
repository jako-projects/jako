package java.time

import java.io.Serializable
import java.math.BigInteger
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.temporal.Temporal
import java.time.temporal.TemporalAmount
import java.time.temporal.TemporalUnit
import java.time.temporal.UnsupportedTemporalTypeException

private val BI_NANOS_PER_SECOND: BigInteger = BigInteger.valueOf(1_000_000_000L)
private val BI_LONG_MIN: BigInteger = BigInteger.valueOf(Long.MIN_VALUE)
private val BI_LONG_MAX: BigInteger = BigInteger.valueOf(Long.MAX_VALUE)

actual class Duration private constructor(
    actual val seconds: Long,
    actual val nano: Int
) : TemporalAmount, Serializable, Comparable<Duration> {

    actual fun getSeconds(): Long = seconds
    actual fun getNano(): Int = nano

    actual fun toDays(): Long = seconds / 86400L
    actual fun toHours(): Long = seconds / 3600L
    actual fun toMinutes(): Long = seconds / 60L

    actual fun toMillis(): Long {
        var tempSeconds = seconds
        var tempNano = nano.toLong()
        if (tempSeconds < 0) {
            tempSeconds += 1L
            tempNano -= 1_000_000_000L
        }
        val millis = multiplyExact(tempSeconds, 1000L)
        return addExact(millis, tempNano / 1_000_000L)
    }

    actual fun toNanos(): Long {
        var tempSeconds = seconds
        var tempNano = nano.toLong()
        if (tempSeconds < 0) {
            tempSeconds += 1L
            tempNano -= 1_000_000_000L
        }
        val totalNanos = multiplyExact(tempSeconds, 1_000_000_000L)
        return addExact(totalNanos, tempNano)
    }

    actual fun plus(duration: Duration): Duration {
        return plus(duration.seconds, duration.nano.toLong())
    }

    actual fun plusDays(daysToAdd: Long): Duration = plus(multiplyExact(daysToAdd, 86400L), 0L)
    actual fun plusHours(hoursToAdd: Long): Duration = plus(multiplyExact(hoursToAdd, 3600L), 0L)
    actual fun plusMinutes(minutesToAdd: Long): Duration = plus(multiplyExact(minutesToAdd, 60L), 0L)
    actual fun plusSeconds(secondsToAdd: Long): Duration = plus(secondsToAdd, 0L)
    actual fun plusMillis(millisToAdd: Long): Duration = plus(millisToAdd / 1000L, (millisToAdd % 1000L) * 1_000_000L)
    actual fun plusNanos(nanosToAdd: Long): Duration = plus(0L, nanosToAdd)

    private fun plus(secondsToAdd: Long, nanosToAdd: Long): Duration {
        if ((secondsToAdd or nanosToAdd) == 0L) return this
        var epochSec = addExact(seconds, secondsToAdd)
        epochSec = addExact(epochSec, nanosToAdd / 1_000_000_000L)
        val newNanos = nano.toLong() + (nanosToAdd % 1_000_000_000L)
        return ofSeconds(epochSec, newNanos)
    }

    actual fun minus(duration: Duration): Duration {
        val secsToSubtract = duration.seconds
        val nanosToSubtract = duration.nano
        if (secsToSubtract == Long.MIN_VALUE) {
            return plus(Long.MAX_VALUE, -nanosToSubtract.toLong()).plus(1L, 0L)
        }
        return plus(-secsToSubtract, -nanosToSubtract.toLong())
    }

    actual fun minusDays(daysToSubtract: Long): Duration =
        if (daysToSubtract == Long.MIN_VALUE) plusDays(Long.MAX_VALUE).plusDays(1L) else plusDays(-daysToSubtract)

    actual fun minusHours(hoursToSubtract: Long): Duration =
        if (hoursToSubtract == Long.MIN_VALUE) plusHours(Long.MAX_VALUE).plusHours(1L) else plusHours(-hoursToSubtract)

    actual fun minusMinutes(minutesToSubtract: Long): Duration =
        if (minutesToSubtract == Long.MIN_VALUE) plusMinutes(Long.MAX_VALUE).plusMinutes(1L) else plusMinutes(-minutesToSubtract)

    actual fun minusSeconds(secondsToSubtract: Long): Duration =
        if (secondsToSubtract == Long.MIN_VALUE) plusSeconds(Long.MAX_VALUE).plusSeconds(1L) else plusSeconds(-secondsToSubtract)

    actual fun minusMillis(millisToSubtract: Long): Duration =
        if (millisToSubtract == Long.MIN_VALUE) plusMillis(Long.MAX_VALUE).plusMillis(1L) else plusMillis(-millisToSubtract)

    actual fun minusNanos(nanosToSubtract: Long): Duration =
        if (nanosToSubtract == Long.MIN_VALUE) plusNanos(Long.MAX_VALUE).plusNanos(1L) else plusNanos(-nanosToSubtract)

    actual fun multipliedBy(multiplicand: Long): Duration {
        if (multiplicand == 0L) return ZERO
        if (multiplicand == 1L) return this
        return fromBigIntegerNanos(toBigIntegerNanos().multiply(BigInteger.valueOf(multiplicand)))
    }

    actual fun dividedBy(divisor: Long): Duration {
        if (divisor == 0L) throw ArithmeticException("Cannot divide by zero")
        if (divisor == 1L) return this
        return fromBigIntegerNanos(toBigIntegerNanos().divide(BigInteger.valueOf(divisor)))
    }

    private fun toBigIntegerNanos(): BigInteger {
        return BigInteger.valueOf(seconds)
            .multiply(BI_NANOS_PER_SECOND)
            .add(BigInteger.valueOf(nano.toLong()))
    }

    private fun fromBigIntegerNanos(totalNanos: BigInteger): Duration {
        val (q, r) = totalNanos.divideAndRemainder(BI_NANOS_PER_SECOND)
        if (q < BI_LONG_MIN || q > BI_LONG_MAX) {
            throw ArithmeticException("Exceeds capacity of Duration: $totalNanos")
        }
        return ofSeconds(q.toLong(), r.toLong())
    }

    actual fun negated(): Duration = multipliedBy(-1L)

    actual fun abs(): Duration = if (isNegative()) negated() else this

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
        val hours = seconds / 3600L
        val minutes = ((seconds % 3600L) / 60L).toInt()
        val secs = (seconds % 60L).toInt()
        val sb = StringBuilder("PT")
        if (hours != 0L) {
            sb.append(hours).append('H')
        }
        if (minutes != 0) {
            sb.append(minutes).append('M')
        }
        if (secs == 0 && nano == 0 && sb.length > 2) {
            return sb.toString()
        }
        if (secs < 0 && nano > 0) {
            if (secs == -1) sb.append("-0") else sb.append(secs + 1)
        } else {
            sb.append(secs)
        }
        if (nano > 0) {
            val pos = sb.length
            if (secs < 0) {
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
            return if ((secs or mos.toLong()) == 0L) ZERO else Duration(secs, mos * 1000000)
        }

        actual fun ofSeconds(seconds: Long): Duration {
            return ofSeconds(seconds, 0L)
        }

        actual fun ofSeconds(seconds: Long, nanoAdjustment: Long): Duration {
            val secs = addExact(seconds, nanoAdjustment.floorDiv(1000000000L))
            val nos = nanoAdjustment.mod(1000000000L).toInt()
            return if ((secs or nos.toLong()) == 0L) ZERO else Duration(secs, nos)
        }

        actual fun ofNanos(nanos: Long): Duration {
            var secs = nanos / 1000000000L
            var nos = (nanos % 1000000000L).toInt()
            if (nos < 0) {
                nos += 1000000000
                secs--
            }
            return if ((secs or nos.toLong()) == 0L) ZERO else Duration(secs, nos)
        }

        actual fun ofMinutes(minutes: Long): Duration = ofSeconds(multiplyExact(minutes, 60L), 0L)

        actual fun ofHours(hours: Long): Duration = ofSeconds(multiplyExact(hours, 3600L), 0L)

        actual fun ofDays(days: Long): Duration = ofSeconds(multiplyExact(days, 86400L), 0L)

        actual fun of(amount: Long, unit: TemporalUnit): Duration = ZERO.plus(amount, unit)

        private fun Duration.plus(amount: Long, unit: TemporalUnit): Duration {
            if (unit == ChronoUnit.DAYS) return plusDays(amount)
            if (unit.isDurationEstimated) throw UnsupportedTemporalTypeException("Unit must not have an estimated duration")
            if (amount == 0L) return this
            if (unit is ChronoUnit) {
                return when (unit) {
                    ChronoUnit.NANOS -> plusNanos(amount)
                    ChronoUnit.MICROS -> plusSeconds((amount / 1_000_000L), (amount % 1_000_000L) * 1000L)
                    ChronoUnit.MILLIS -> plusMillis(amount)
                    ChronoUnit.SECONDS -> plusSeconds(amount)
                    ChronoUnit.MINUTES -> plusMinutes(amount)
                    ChronoUnit.HOURS -> plusHours(amount)
                    ChronoUnit.HALF_DAYS -> plusHours(multiplyExact(amount, 12L))
                    else -> plusSeconds(multiplyExact(unit.duration.seconds, amount))
                }
            }
            val dur = unit.duration.multipliedBy(amount)
            return plusSeconds(dur.seconds).plusNanos(dur.nano.toLong())
        }

        private fun plusSeconds(secondsToAdd: Long, nanosToAdd: Long): Duration =
            ofSeconds(secondsToAdd, nanosToAdd)

        actual fun between(startInclusive: Temporal, endExclusive: Temporal): Duration {
            return when {
                startInclusive is Instant && endExclusive is Instant -> {
                    val secs = addExact(endExclusive.epochSecond, -startInclusive.epochSecond)
                    val nanos = (endExclusive.nano - startInclusive.nano).toLong()
                    ofSeconds(secs, nanos)
                }
                startInclusive is LocalTime && endExclusive is LocalTime -> {
                    ofNanos(endExclusive.toNanoOfDay() - startInclusive.toNanoOfDay())
                }
                startInclusive is LocalDateTime && endExclusive is LocalDateTime -> {
                    val startSecs = startInclusive.toEpochSecond(ZoneOffset.UTC)
                    val endSecs = endExclusive.toEpochSecond(ZoneOffset.UTC)
                    ofSeconds(addExact(endSecs, -startSecs), (endExclusive.nano - startInclusive.nano).toLong())
                }
                startInclusive is OffsetDateTime && endExclusive is OffsetDateTime -> {
                    between(startInclusive.toInstant(), endExclusive.toInstant())
                }
                startInclusive is ZonedDateTime && endExclusive is ZonedDateTime -> {
                    between(startInclusive.toInstant(), endExclusive.toInstant())
                }
                else -> {
                    val nanos = ChronoUnit.NANOS.between(startInclusive, endExclusive)
                    ofNanos(nanos)
                }
            }
        }

        actual fun parse(text: CharSequence): Duration {
            val s = text.toString()
            if (s.length < 3) throw DateTimeParseException("Text cannot be parsed to a Duration", s, 0)
            var idx = 0
            var negative = false
            if (s[idx] == '+' || s[idx] == '-') {
                negative = s[idx] == '-'
                idx++
            }
            if (idx >= s.length || (s[idx] != 'P' && s[idx] != 'p')) {
                throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx)
            }
            idx++
            var inTime = false
            var daysSecs = 0L
            var hoursSecs = 0L
            var minsSecs = 0L
            var secs = 0L
            var nanos = 0
            var parsedAny = false
            var lastUnitOrder = 0 // 1=D, 2=H, 3=M, 4=S
            try {
                while (idx < s.length) {
                    if (s[idx] == 'T' || s[idx] == 't') {
                        if (inTime) throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx)
                        inTime = true
                        idx++
                        if (idx >= s.length) throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx)
                    }
                    var sign = 1L
                    if (s[idx] == '+' || s[idx] == '-') {
                        if (s[idx] == '-') sign = -1L
                        idx++
                    }
                    val numStart = idx
                    while (idx < s.length && s[idx] in '0'..'9') idx++
                    if (idx == numStart) throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx)
                    val num = s.substring(numStart, idx).toLongOrNull()
                        ?: throw DateTimeParseException("Text cannot be parsed to a Duration", s, numStart)
                    var fracNanos = 0
                    var hasFrac = false
                    if (idx < s.length && (s[idx] == '.' || s[idx] == ',')) {
                        hasFrac = true
                        idx++
                        val fracStart = idx
                        while (idx < s.length && s[idx] in '0'..'9') idx++
                        val fracStr = s.substring(fracStart, idx)
                        if (fracStr.isEmpty() || fracStr.length > 9) {
                            throw DateTimeParseException("Text cannot be parsed to a Duration", s, fracStart)
                        }
                        fracNanos = fracStr.padEnd(9, '0').toInt()
                    }
                    if (idx >= s.length) throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx)
                    val unitChar = s[idx].uppercaseChar()
                    idx++
                    parsedAny = true
                    when (unitChar) {
                        'D' -> {
                            if (inTime || hasFrac || lastUnitOrder >= 1) {
                                throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx - 1)
                            }
                            lastUnitOrder = 1
                            daysSecs = multiplyExact(multiplyExact(num, sign), 86400L)
                        }
                        'H' -> {
                            if (!inTime || hasFrac || lastUnitOrder >= 2) {
                                throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx - 1)
                            }
                            lastUnitOrder = 2
                            hoursSecs = multiplyExact(multiplyExact(num, sign), 3600L)
                        }
                        'M' -> {
                            if (!inTime || hasFrac || lastUnitOrder >= 3) {
                                throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx - 1)
                            }
                            lastUnitOrder = 3
                            minsSecs = multiplyExact(multiplyExact(num, sign), 60L)
                        }
                        'S' -> {
                            if (!inTime || lastUnitOrder >= 4) {
                                throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx - 1)
                            }
                            lastUnitOrder = 4
                            secs = multiplyExact(num, sign)
                            nanos = if (sign < 0) -fracNanos else fracNanos
                        }
                        else -> throw DateTimeParseException("Text cannot be parsed to a Duration", s, idx - 1)
                    }
                }
                if (!parsedAny) throw DateTimeParseException("Text cannot be parsed to a Duration", s, 0)
                val totalSecs = addExact(addExact(daysSecs, hoursSecs), addExact(minsSecs, secs))
                val result = ofSeconds(totalSecs, nanos.toLong())
                return if (negative) result.negated() else result
            } catch (ex: DateTimeParseException) {
                throw ex
            } catch (ex: ArithmeticException) {
                throw DateTimeParseException("Text cannot be parsed to a Duration: overflow", s, 0)
            }
        }
    }
}

