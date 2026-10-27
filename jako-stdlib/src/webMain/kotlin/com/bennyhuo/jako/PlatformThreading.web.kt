package com.bennyhuo.jako

import java.lang.Thread

internal actual fun isMultiThreadingSupported(): Boolean = false

internal actual fun platformSleepSliceMillis(ms: Int) {
    if (ms > 0) {
        NodeBridge.sleepMillis(ms)
    }
}

/**
 * Cross-module bridge for `jako-android` (`webMain`) to access Node.js process metadata
 * and cooperative `HandlerThread` thread-context switching.
 */
object WebPlatform {
    fun pid(): Int = NodeBridge.pid()
    fun uid(): Int = NodeBridge.uid()

    fun markThreadRunning(thread: Thread) {
        thread.markAttachedAndRunning()
    }

    fun markThreadTerminated(thread: Thread) {
        thread.markTerminated()
    }

    fun <T> withCurrentThread(thread: Thread, block: () -> T): T {
        return Thread.withCurrentThread(thread, block)
    }
}
