package java.nio.channels

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual abstract class SelectionKey protected actual constructor() {
    private val attLock = ReentrantLock()
    private var attachedObj: Any? = null

    actual abstract fun channel(): SelectableChannel
    actual abstract fun selector(): Selector
    actual abstract fun isValid(): Boolean
    actual abstract fun cancel()
    actual abstract fun interestOps(): Int
    actual abstract fun interestOps(ops: Int): SelectionKey
    actual abstract fun readyOps(): Int

    actual fun isReadable(): Boolean {
        if (!isValid()) throw CancelledKeyException()
        return (readyOps() and OP_READ) != 0
    }

    actual fun isWritable(): Boolean {
        if (!isValid()) throw CancelledKeyException()
        return (readyOps() and OP_WRITE) != 0
    }

    actual fun isConnectable(): Boolean {
        if (!isValid()) throw CancelledKeyException()
        return (readyOps() and OP_CONNECT) != 0
    }

    actual fun isAcceptable(): Boolean {
        if (!isValid()) throw CancelledKeyException()
        return (readyOps() and OP_ACCEPT) != 0
    }

    actual fun attach(ob: Any?): Any? = attLock.withLock {
        val prev = attachedObj
        attachedObj = ob
        prev
    }

    actual fun attachment(): Any? = attLock.withLock { attachedObj }

    actual companion object {
        actual val OP_READ: Int = 1 shl 0
        actual val OP_WRITE: Int = 1 shl 2
        actual val OP_CONNECT: Int = 1 shl 3
        actual val OP_ACCEPT: Int = 1 shl 4
    }
}
