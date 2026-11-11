package android.os

final class SystemClock private constructor() {
    companion object {
        @JvmStatic
        fun sleep(ms: Long) {
            val start = uptimeMillis()
            var duration = ms
            var interrupted = false
            do {
                try {
                    Thread.sleep(duration)
                } catch (_: InterruptedException) {
                    interrupted = true
                }
                duration = start + ms - uptimeMillis()
            } while (duration > 0)
            if (interrupted) {
                Thread.currentThread().interrupt()
            }
        }

        @JvmStatic
        fun uptimeMillis(): Long = System.nanoTime() / 1_000_000L

        @JvmStatic
        fun elapsedRealtime(): Long = System.nanoTime() / 1_000_000L

        @JvmStatic
        fun elapsedRealtimeNanos(): Long = System.nanoTime()
    }
}
