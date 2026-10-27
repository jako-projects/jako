package java.time.temporal

import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.OffsetTime
import java.time.Year
import java.time.YearMonth
import java.time.ZonedDateTime
import java.time.addExact
import java.time.isLeapYear
import java.time.lengthOfMonth

actual enum class ChronoUnit(
    private val unitDuration: Duration,
    private val estimated: Boolean,
    private val dateBased: Boolean,
    private val timeBased: Boolean
) : TemporalUnit {
    NANOS(Duration.ofNanos(1), false, false, true),
    MICROS(Duration.ofNanos(1_000), false, false, true),
    MILLIS(Duration.ofNanos(1_000_000), false, false, true),
    SECONDS(Duration.ofSeconds(1), false, false, true),
    MINUTES(Duration.ofSeconds(60), false, false, true),
    HOURS(Duration.ofSeconds(3600), false, false, true),
    HALF_DAYS(Duration.ofSeconds(43200), false, false, true),
    DAYS(Duration.ofSeconds(86400), true, true, false),
    WEEKS(Duration.ofSeconds(7 * 86400L), true, true, false),
    MONTHS(Duration.ofSeconds(31556952L / 12), true, true, false),
    YEARS(Duration.ofSeconds(31556952L), true, true, false),
    DECADES(Duration.ofSeconds(31556952L * 10L), true, true, false),
    CENTURIES(Duration.ofSeconds(31556952L * 100L), true, true, false),
    MILLENNIA(Duration.ofSeconds(31556952L * 1000L), true, true, false),
    ERAS(Duration.ofSeconds(31556952L * 1_000_000_000L), true, true, false),
    FOREVER(Duration.ofSeconds(Long.MAX_VALUE, 999_999_999L), true, false, false);

    actual override val duration: Duration get() = unitDuration
    actual override val isDurationEstimated: Boolean get() = estimated
    actual override val isDateBased: Boolean get() = dateBased
    actual override val isTimeBased: Boolean get() = timeBased
    actual override fun getDuration(): Duration = unitDuration
    actual override fun isDurationEstimated(): Boolean = estimated
    actual override fun isDateBased(): Boolean = dateBased
    actual override fun isTimeBased(): Boolean = timeBased

    actual override fun isSupportedBy(temporal: Temporal): Boolean {
        if (this == FOREVER) return false
        return when (temporal) {
            is LocalDate -> dateBased
            is LocalTime, is OffsetTime -> timeBased
            is LocalDateTime, is OffsetDateTime, is ZonedDateTime -> dateBased || timeBased
            is Instant -> timeBased || this == DAYS
            is Year -> this == YEARS || this == DECADES || this == CENTURIES || this == MILLENNIA || this == ERAS
            is YearMonth -> this == MONTHS || this == YEARS || this == DECADES || this == CENTURIES || this == MILLENNIA || this == ERAS
            else -> false
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual override fun <R : Temporal> addTo(temporal: R, amount: Long): R {
        return when (temporal) {
            is LocalDate -> temporal.plus(amount, this) as R
            is LocalTime -> temporal.plus(amount, this) as R
            is LocalDateTime -> temporal.plus(amount, this) as R
            is Instant -> temporal.plus(amount, this) as R
            is OffsetDateTime -> OffsetDateTime.of(temporal.toLocalDateTime().plus(amount, this), temporal.offset) as R
            is ZonedDateTime -> ZonedDateTime.of(temporal.toLocalDateTime().plus(amount, this), temporal.zone) as R
            is Year -> when (this) {
                YEARS -> temporal.plusYears(amount) as R
                DECADES -> temporal.plusYears(amount * 10L) as R
                CENTURIES -> temporal.plusYears(amount * 100L) as R
                MILLENNIA -> temporal.plusYears(amount * 1000L) as R
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $this")
            }
            is YearMonth -> when (this) {
                MONTHS -> temporal.plusMonths(amount) as R
                YEARS -> temporal.plusYears(amount) as R
                DECADES -> temporal.plusYears(amount * 10L) as R
                CENTURIES -> temporal.plusYears(amount * 100L) as R
                MILLENNIA -> temporal.plusYears(amount * 1000L) as R
                else -> throw UnsupportedTemporalTypeException("Unsupported unit: $this")
            }
            else -> throw UnsupportedTemporalTypeException("Unsupported temporal for addTo: $temporal")
        }
    }

    actual override fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long {
        return when {
            temporal1Inclusive is LocalDate && temporal2Exclusive is LocalDate ->
                temporal1Inclusive.until(temporal2Exclusive, this)
            temporal1Inclusive is LocalTime && temporal2Exclusive is LocalTime ->
                temporal1Inclusive.until(temporal2Exclusive, this)
            temporal1Inclusive is LocalDateTime && temporal2Exclusive is LocalDateTime ->
                temporal1Inclusive.until(temporal2Exclusive, this)
            temporal1Inclusive is Instant && temporal2Exclusive is Instant ->
                temporal1Inclusive.until(temporal2Exclusive, this)
            temporal1Inclusive is OffsetDateTime && temporal2Exclusive is OffsetDateTime ->
                temporal1Inclusive.toInstant().until(temporal2Exclusive.toInstant(), this)
            temporal1Inclusive is ZonedDateTime && temporal2Exclusive is ZonedDateTime ->
                temporal1Inclusive.toInstant().until(temporal2Exclusive.toInstant(), this)
            else -> throw UnsupportedTemporalTypeException("Unsupported temporals for between: $temporal1Inclusive, $temporal2Exclusive")
        }
    }
}
