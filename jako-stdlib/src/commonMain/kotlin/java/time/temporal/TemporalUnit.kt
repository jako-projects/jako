@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

import java.time.Duration

/**
 * A unit of date-time, such as Days or Hours.
 */
expect interface TemporalUnit {
    val duration: Duration
    val isDurationEstimated: Boolean
    val isDateBased: Boolean
    val isTimeBased: Boolean
    fun getDuration(): Duration
    fun isDurationEstimated(): Boolean
    fun isDateBased(): Boolean
    fun isTimeBased(): Boolean
    fun isSupportedBy(temporal: Temporal): Boolean
    fun <R : Temporal> addTo(temporal: R, amount: Long): R
    fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long
}
