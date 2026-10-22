@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

import java.time.Duration

/**
 * A standard set of date periods units.
 */
expect enum class ChronoUnit : TemporalUnit {
    NANOS,
    MICROS,
    MILLIS,
    SECONDS,
    MINUTES,
    HOURS,
    HALF_DAYS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS,
    DECADES,
    CENTURIES,
    MILLENNIA,
    ERAS,
    FOREVER;

    override val duration: Duration
    override val isDurationEstimated: Boolean
    override val isDateBased: Boolean
    override val isTimeBased: Boolean
    override fun getDuration(): Duration
    override fun isDurationEstimated(): Boolean
    override fun isDateBased(): Boolean
    override fun isTimeBased(): Boolean
    override fun isSupportedBy(temporal: Temporal): Boolean
    override fun <R : Temporal> addTo(temporal: R, amount: Long): R
    override fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long
}
