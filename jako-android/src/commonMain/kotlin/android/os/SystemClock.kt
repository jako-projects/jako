@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Core timekeeping facilities matching `android.os.SystemClock`.
 */
expect final class SystemClock {
    companion object {
        fun sleep(ms: Long)
        fun uptimeMillis(): Long
        fun elapsedRealtime(): Long
        fun elapsedRealtimeNanos(): Long
    }
}
