package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir
import kotlinx.cinterop.sizeOf
import platform.posix.getpid

actual final class SystemClock private constructor() {
    actual companion object {
        actual fun sleep(ms: Long) {
            var start = uptimeMillis()
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

        actual fun uptimeMillis(): Long = System.nanoTime() / 1_000_000L

        actual fun elapsedRealtime(): Long = System.nanoTime() / 1_000_000L

        actual fun elapsedRealtimeNanos(): Long = System.nanoTime()
    }
}
