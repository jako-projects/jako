@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.time.temporal

/**
 * Strategy for adjusting a temporal object.
 */
expect fun interface TemporalAdjuster {
    fun adjustInto(temporal: Temporal): Temporal
}
