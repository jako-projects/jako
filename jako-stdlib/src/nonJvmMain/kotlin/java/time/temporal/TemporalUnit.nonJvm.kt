package java.time.temporal

import java.time.Duration
import java.time.addExact
import java.time.isLeapYear
import java.time.lengthOfMonth

actual interface TemporalUnit {
    actual val duration: Duration
    actual val isDurationEstimated: Boolean
    actual val isDateBased: Boolean
    actual val isTimeBased: Boolean
    actual fun getDuration(): Duration
    actual fun isDurationEstimated(): Boolean
    actual fun isDateBased(): Boolean
    actual fun isTimeBased(): Boolean
    actual fun isSupportedBy(temporal: Temporal): Boolean
    actual fun <R : Temporal> addTo(temporal: R, amount: Long): R
    actual fun between(temporal1Inclusive: Temporal, temporal2Exclusive: Temporal): Long
}
