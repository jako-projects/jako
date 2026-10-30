package java.nio.channels

import com.bennyhuo.jako.NodeBridge
import com.bennyhuo.jako.WebTaskScheduler
import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.requireWebSupport
import java.net.DatagramSocket
import java.net.ServerSocket
import java.net.Socket

internal actual fun getSocketNativeHandle(socket: Socket): Long =
    if (socket.isClosed() || socket.handle < 0) -1L else socket.handle.toLong()

internal actual fun getServerSocketNativeHandle(serverSocket: ServerSocket): Long =
    if (serverSocket.isClosed() || serverSocket.handle < 0) -1L else serverSocket.handle.toLong()

internal actual fun getDatagramSocketNativeHandle(datagramSocket: DatagramSocket): Long =
    if (datagramSocket.isClosed() || datagramSocket.handle < 0) -1L else datagramSocket.handle.toLong()

internal actual fun pollNativeSocketHandle(handle: Long, interestOps: Int): Int {
    if (handle < 0L || interestOps == 0) return 0
    requireWebSupport(
        api = "java.nio.channels.Selector",
        WebRuntime.NODE_JS,
        reason = "Socket channel readiness polling requires Node.js worker_threads"
    )
    if (WebTaskScheduler.hasPendingWork()) {
        WebTaskScheduler.runAllAvailable(8)
    }
    return NodeBridge.pollHandle(handle.toInt(), interestOps)
}

internal actual class NativeSelectorMux actual constructor() {
    private var awakened = false

    actual fun await(handles: LongArray, interestOps: IntArray, count: Int, timeoutMs: Int): Boolean {
        if (WebTaskScheduler.hasPendingWork()) {
            WebTaskScheduler.runAllAvailable(16)
        }
        if (awakened) {
            awakened = false
            return true
        }
        if (count == 0) {
            if (timeoutMs != 0) {
                val effectiveWait = if (timeoutMs < 0) 1000 else timeoutMs
                val sliceMs = if (WebTaskScheduler.hasPendingWork()) {
                    effectiveWait.coerceAtMost(10)
                } else {
                    effectiveWait.coerceAtMost(50)
                }
                NodeBridge.sleepMillis(sliceMs)
                if (WebTaskScheduler.hasPendingWork()) {
                    WebTaskScheduler.runAllAvailable(16)
                }
            }
            return awakened.also { awakened = false }
        }
        requireWebSupport(
            api = "java.nio.channels.Selector",
            WebRuntime.NODE_JS,
            reason = "Socket channel readiness polling requires Node.js worker_threads"
        )
        val packed = buildString {
            for (i in 0 until count) {
                if (i > 0) append(',')
                append(handles[i]).append(':').append(interestOps[i])
            }
        }
        if (timeoutMs == 0) {
            return NodeBridge.selectorWait(packed, 0)
        }
        val effectiveWait = if (timeoutMs < 0) 1000 else timeoutMs
        val sliceMs = if (WebTaskScheduler.hasPendingWork()) {
            effectiveWait.coerceAtMost(10)
        } else {
            effectiveWait.coerceAtMost(200)
        }
        val ready = NodeBridge.selectorWait(packed, sliceMs)
        if (WebTaskScheduler.hasPendingWork()) {
            WebTaskScheduler.runAllAvailable(16)
        }
        if (ready) return true
        return awakened.also { awakened = false }
    }

    actual fun wakeup() {
        awakened = true
    }

    actual fun clearWakeup() {
        awakened = false
    }

    actual fun close() {
        awakened = false
    }
}
