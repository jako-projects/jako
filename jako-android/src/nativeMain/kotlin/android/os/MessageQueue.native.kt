package android.os

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual final class MessageQueue internal constructor(private val quitAllowed: Boolean) {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var messages: Message? = null
    private val idleHandlers = ArrayList<IdleHandler>()
    private var quitting = false

    actual val isIdle: Boolean
        get() = isIdle()

    actual fun isIdle(): Boolean = lock.withLock {
        val now = SystemClock.uptimeMillis()
        messages == null || now < messages!!._when
    }

    actual fun addIdleHandler(handler: IdleHandler) {
        lock.withLock {
            idleHandlers.add(handler)
        }
    }

    actual fun removeIdleHandler(handler: IdleHandler) {
        lock.withLock {
            idleHandlers.remove(handler)
        }
    }

    internal fun next(): Message? {
        var pendingIdleHandlerCount = -1
        var nextPollTimeoutMillis = 0L

        while (true) {
            var idleHandlersSnapshot: List<IdleHandler>? = null
            lock.withLock {
                if (nextPollTimeoutMillis > 0) {
                    condition.awaitNanos(nextPollTimeoutMillis * 1_000_000L)
                } else if (nextPollTimeoutMillis < 0) {
                    condition.await()
                }

                val now = SystemClock.uptimeMillis()
                val msg = messages
                if (msg != null) {
                    if (now < msg._when) {
                        nextPollTimeoutMillis = msg._when - now
                    } else {
                        messages = msg.next
                        msg.next = null
                        msg.inUse = true
                        return msg
                    }
                } else {
                    nextPollTimeoutMillis = -1L
                }

                if (quitting) {
                    return null
                }

                if (pendingIdleHandlerCount < 0 && (messages == null || now < messages!!._when)) {
                    pendingIdleHandlerCount = idleHandlers.size
                }
                if (pendingIdleHandlerCount <= 0) {
                    return@withLock
                }
                idleHandlersSnapshot = ArrayList(idleHandlers)
            }

            val handlers = idleHandlersSnapshot
            if (handlers != null) {
                for (idler in handlers) {
                    val keep = try {
                        idler.queueIdle()
                    } catch (_: Throwable) {
                        false
                    }
                    if (!keep) {
                        lock.withLock {
                            idleHandlers.remove(idler)
                        }
                    }
                }
                pendingIdleHandlerCount = 0
                nextPollTimeoutMillis = 0L
            }
        }
    }

    internal fun quit(safe: Boolean) {
        if (!quitAllowed) {
            throw IllegalStateException("Main thread not allowed to quit.")
        }
        lock.withLock {
            if (quitting) return
            quitting = true
            if (safe) {
                removeAllFutureMessagesLocked()
            } else {
                removeAllMessagesLocked()
            }
            condition.signalAll()
        }
    }

    internal fun enqueueMessage(msg: Message, whenMillis: Long): Boolean {
        if (msg.target == null) {
            throw IllegalArgumentException("Message must have a target.")
        }
        lock.withLock {
            if (msg.inUse) {
                throw IllegalStateException("$msg This message is already in use.")
            }
            if (quitting) {
                msg.recycle()
                return false
            }
            msg.inUse = true
            msg._when = whenMillis
            var p = messages
            if (p == null || whenMillis == 0L || whenMillis < p._when) {
                msg.next = p
                messages = msg
            } else {
                var prev: Message
                while (true) {
                    prev = p!!
                    p = p.next
                    if (p == null || whenMillis < p._when) {
                        break
                    }
                }
                msg.next = p
                prev.next = msg
            }
            condition.signalAll()
            return true
        }
    }

    internal fun hasMessages(h: Handler, what: Int, `object`: Any?): Boolean = lock.withLock {
        var p = messages
        while (p != null) {
            if (p.target === h && p.what == what && (`object` == null || p.obj == `object`)) {
                return true
            }
            p = p.next
        }
        false
    }

    internal fun hasMessages(h: Handler, r: Runnable, `object`: Any?): Boolean = lock.withLock {
        var p = messages
        while (p != null) {
            if (p.target === h && p._callback === r && (`object` == null || p.obj == `object`)) {
                return true
            }
            p = p.next
        }
        false
    }

    internal fun removeMessages(h: Handler, what: Int, `object`: Any?) {
        lock.withLock {
            var p = messages
            while (p != null && p.target === h && p.what == what && (`object` == null || p.obj == `object`)) {
                val n = p.next
                messages = n
                p.recycle()
                p = n
            }
            while (p != null) {
                val n = p.next
                if (n != null && n.target === h && n.what == what && (`object` == null || n.obj == `object`)) {
                    val nn = n.next
                    n.recycle()
                    p.next = nn
                    continue
                }
                p = n
            }
        }
    }

    internal fun removeMessages(h: Handler, r: Runnable, `object`: Any?) {
        lock.withLock {
            var p = messages
            while (p != null && p.target === h && p._callback === r && (`object` == null || p.obj == `object`)) {
                val n = p.next
                messages = n
                p.recycle()
                p = n
            }
            while (p != null) {
                val n = p.next
                if (n != null && n.target === h && n._callback === r && (`object` == null || n.obj == `object`)) {
                    val nn = n.next
                    n.recycle()
                    p.next = nn
                    continue
                }
                p = n
            }
        }
    }

    internal fun removeCallbacksAndMessages(h: Handler, `object`: Any?) {
        lock.withLock {
            var p = messages
            while (p != null && p.target === h && (`object` == null || p.obj == `object`)) {
                val n = p.next
                messages = n
                p.recycle()
                p = n
            }
            while (p != null) {
                val n = p.next
                if (n != null && n.target === h && (`object` == null || n.obj == `object`)) {
                    val nn = n.next
                    n.recycle()
                    p.next = nn
                    continue
                }
                p = n
            }
        }
    }

    private fun removeAllMessagesLocked() {
        var p = messages
        while (p != null) {
            val n = p.next
            p.recycle()
            p = n
        }
        messages = null
    }

    private fun removeAllFutureMessagesLocked() {
        val now = SystemClock.uptimeMillis()
        var p = messages
        if (p != null) {
            if (p._when > now) {
                removeAllMessagesLocked()
            } else {
                while (true) {
                    val n = p!!.next ?: return
                    if (n._when > now) break
                    p = n
                }
                var n = p!!.next
                p.next = null
                while (n != null) {
                    val next = n.next
                    n.recycle()
                    n = next
                }
            }
        }
    }

    actual fun interface IdleHandler {
        actual fun queueIdle(): Boolean
    }
}
