@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.experimental.ExperimentalNativeApi::class)

package java.lang

import java.io.InputStream
import java.io.OutputStream
import java.util.concurrent.TimeUnit
import kotlin.concurrent.withLock

actual abstract class Process actual constructor() {
    actual abstract fun getOutputStream(): OutputStream
    actual abstract fun getInputStream(): InputStream
    actual abstract fun getErrorStream(): InputStream
    actual abstract fun waitFor(): Int

    actual open fun waitFor(timeout: Long, unit: TimeUnit): Boolean {
        val startNanos = System.nanoTime()
        var rem = unit.toNanos(timeout)
        while (true) {
            try {
                exitValue()
                return true
            } catch (_: IllegalThreadStateException) {
                if (rem <= 0L) return false
                val sleepMs = minOf(TimeUnit.NANOSECONDS.toMillis(rem) + 1L, 20L)
                Thread.sleep(sleepMs)
                rem = unit.toNanos(timeout) - (System.nanoTime() - startNanos)
            }
        }
    }

    actual abstract fun exitValue(): Int
    actual abstract fun destroy()

    actual open fun destroyForcibly(): Process {
        destroy()
        return this
    }

    actual open fun isAlive(): Boolean {
        return try {
            exitValue()
            false
        } catch (_: IllegalThreadStateException) {
            true
        }
    }

    actual open val outputStream: OutputStream get() = getOutputStream()
    actual open val inputStream: InputStream get() = getInputStream()
    actual open val errorStream: InputStream get() = getErrorStream()
    actual open val isAlive: Boolean get() = isAlive()
}
