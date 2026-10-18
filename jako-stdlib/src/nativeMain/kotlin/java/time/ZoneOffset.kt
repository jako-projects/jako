package java.time

import java.io.Serializable
import java.time.temporal.Temporal
import java.time.temporal.TemporalAccessor
import java.time.temporal.TemporalAdjuster
import kotlin.math.abs

actual class ZoneOffset private constructor(
    actual val totalSeconds: Int,
    private val offsetId: String
) : ZoneId(), TemporalAccessor, TemporalAdjuster, Comparable<ZoneOffset>, Serializable {

    actual override val id: String get() = offsetId
    actual override fun getId(): String = offsetId
    actual fun getTotalSeconds(): Int = totalSeconds

    override fun offsetAt(instant: Instant): ZoneOffset = this
    override fun offsetAt(localDateTime: LocalDateTime): ZoneOffset = this

    actual override fun adjustInto(temporal: Temporal): Temporal {
        return when (temporal) {
            is OffsetDateTime -> OffsetDateTime.of(temporal.toLocalDateTime(), this)
            is OffsetTime -> OffsetTime.of(temporal.toLocalTime(), this)
            else -> throw DateTimeException("Unable to adjust temporal with ZoneOffset: $temporal")
        }
    }

    actual override fun compareTo(other: ZoneOffset): Int = other.totalSeconds - totalSeconds

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ZoneOffset) return false
        return totalSeconds == other.totalSeconds
    }

    override fun hashCode(): Int = totalSeconds

    override fun toString(): String = offsetId

    actual companion object {
        actual val UTC: ZoneOffset = create(0)
        actual val MIN: ZoneOffset = ofTotalSeconds(-18 * 3600)
        actual val MAX: ZoneOffset = ofTotalSeconds(18 * 3600)

        actual fun of(offsetId: String): ZoneOffset {
            // [Nullability Decision] offsetId is non-null, matching JDK 8 ZoneOffset.of(String)
            if (offsetId == "Z") {
                return UTC
            }
            var id = offsetId
            when (id.length) {
                2 -> id = id[0] + "0" + id[1]
                3, 5, 6, 7, 9 -> {}
                else -> throw DateTimeException("Invalid ID for ZoneOffset, invalid format: $offsetId")
            }
            val first = id[0]
            if (first != '+' && first != '-') {
                throw DateTimeException("Invalid ID for ZoneOffset, plus/minus not found when expected: $offsetId")
            }
            val hours: Int
            val minutes: Int
            val seconds: Int
            try {
                when (id.length) {
                    3 -> {
                        hours = parseNumber(id, 1, false)
                        minutes = 0
                        seconds = 0
                    }
                    5 -> {
                        hours = parseNumber(id, 1, false)
                        minutes = parseNumber(id, 3, false)
                        seconds = 0
                    }
                    6 -> {
                        hours = parseNumber(id, 1, false)
                        minutes = parseNumber(id, 4, true)
                        seconds = 0
                    }
                    7 -> {
                        hours = parseNumber(id, 1, false)
                        minutes = parseNumber(id, 3, false)
                        seconds = parseNumber(id, 5, false)
                    }
                    9 -> {
                        hours = parseNumber(id, 1, false)
                        minutes = parseNumber(id, 4, true)
                        seconds = parseNumber(id, 7, true)
                    }
                    else -> throw DateTimeException("Invalid ID for ZoneOffset, invalid format: $offsetId")
                }
            } catch (ex: DateTimeException) {
                throw DateTimeException("Invalid ID for ZoneOffset, non numeric characters found: $offsetId")
            }
            return if (first == '-') {
                ofHoursMinutesSeconds(-hours, -minutes, -seconds)
            } else {
                ofHoursMinutesSeconds(hours, minutes, seconds)
            }
        }

        actual fun ofHours(hours: Int): ZoneOffset = ofHoursMinutesSeconds(hours, 0, 0)

        actual fun ofHoursMinutes(hours: Int, minutes: Int): ZoneOffset =
            ofHoursMinutesSeconds(hours, minutes, 0)

        actual fun ofHoursMinutesSeconds(hours: Int, minutes: Int, seconds: Int): ZoneOffset {
            validate(hours, minutes, seconds)
            return ofTotalSeconds(hours * 3600 + minutes * 60 + seconds)
        }

        actual fun ofTotalSeconds(totalSeconds: Int): ZoneOffset {
            if (totalSeconds < -18 * 3600 || totalSeconds > 18 * 3600) {
                throw DateTimeException("Zone offset not in valid range: -18:00 to +18:00")
            }
            if (totalSeconds == 0) {
                return UTC
            }
            return create(totalSeconds)
        }

        private fun create(totalSeconds: Int): ZoneOffset {
            return ZoneOffset(totalSeconds, buildId(totalSeconds))
        }

        private fun buildId(totalSeconds: Int): String {
            if (totalSeconds == 0) {
                return "Z"
            }
            val absTotalSeconds = abs(totalSeconds)
            val absHours = absTotalSeconds / 3600
            val absMinutes = (absTotalSeconds / 60) % 60
            val buf = StringBuilder()
                .append(if (totalSeconds < 0) "-" else "+")
                .append(if (absHours < 10) "0" else "").append(absHours)
                .append(if (absMinutes < 10) ":0" else ":").append(absMinutes)
            val absSeconds = absTotalSeconds % 60
            if (absSeconds != 0) {
                buf.append(if (absSeconds < 10) ":0" else ":").append(absSeconds)
            }
            return buf.toString()
        }

        private fun parseNumber(offsetId: String, pos: Int, precededByColon: Boolean): Int {
            if (precededByColon && offsetId[pos - 1] != ':') {
                throw DateTimeException("Invalid ID for ZoneOffset, colon not found when expected: $offsetId")
            }
            val ch1 = offsetId[pos]
            val ch2 = offsetId[pos + 1]
            if (ch1 < '0' || ch1 > '9' || ch2 < '0' || ch2 > '9') {
                throw DateTimeException("Invalid ID for ZoneOffset, non numeric characters found: $offsetId")
            }
            return (ch1 - '0') * 10 + (ch2 - '0')
        }

        private fun validate(hours: Int, minutes: Int, seconds: Int) {
            if (hours < -18 || hours > 18) {
                throw DateTimeException("Zone offset hours not in valid range: value $hours is not in the range -18 to 18")
            }
            if (hours > 0) {
                if (minutes < 0 || seconds < 0) {
                    throw DateTimeException("Zone offset minutes and seconds must be positive because hours is positive")
                }
            } else if (hours < 0) {
                if (minutes > 0 || seconds > 0) {
                    throw DateTimeException("Zone offset minutes and seconds must be negative because hours is negative")
                }
            } else if ((minutes > 0 && seconds < 0) || (minutes < 0 && seconds > 0)) {
                throw DateTimeException("Zone offset minutes and seconds must have the same sign")
            }
            if (abs(minutes) > 59) {
                throw DateTimeException("Zone offset minutes not in valid range: abs(value) ${abs(minutes)} is not in the range 0 to 59")
            }
            if (abs(seconds) > 59) {
                throw DateTimeException("Zone offset seconds not in valid range: abs(value) ${abs(seconds)} is not in the range 0 to 59")
            }
            if (abs(hours) == 18 && (minutes != 0 || seconds != 0)) {
                throw DateTimeException("Zone offset not in valid range: -18:00 to +18:00")
            }
        }
    }
}
