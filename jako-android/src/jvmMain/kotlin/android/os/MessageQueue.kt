package android.os

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class MessageQueue internal constructor(private val quitAllowed: Boolean) {
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    private var messages: Message? = null
    private val idleHandlers = ArrayList<IdleHandler>()
    private var quitting = false

    val isIdle: Boolean
        get() {
            lock.withLock {
                val now = SystemClock.uptimeMillis()
                val head = messages
                return head == null || now < head._when
            }
        }

    fun addIdleHandler(handler: IdleHandler) {
        lock.withLock {
            idleHandlers.add(handler)
        }
    }

    fun removeIdleHandler(handler: IdleHandler) {
        lock.withLock {
            idleHandlers.remove(handler)
        }
    }

    internal fun next(): Message? {
        var pendingIdleHandlerCount = -1
        var nextPollTimeoutMillis = 0L

        while (true) {
            var idleSnapshot: List<IdleHandler>? = null
            lock.withLock {
                if (nextPollTimeoutMillis > 0L) {
                    condition.awaitNanos(nextPollTimeoutMillis * 1_000_000L)
                } else if (nextPollTimeoutMillis < 0L) {
                    if (!quitting && messages == null) {
                        condition.await()
                    }
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

                if (quitting) return null

                if (pendingIdleHandlerCount < 0 && (messages == null || now < messages!!._when)) {
                    pendingIdleHandlerCount = idleHandlers.size
                }
                if (pendingIdleHandlerCount <= 0) {
                    continue
                }
                idleSnapshot = ArrayList(idleHandlers)
            }

            val snapshot = idleSnapshot ?: continue
            for (idler in snapshot) {
                var keep = false
                try {
                    keep = idler.queueIdle()
                } catch (_: Throwable) {
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

    internal fun enqueueMessage(msg: Message, `when`: Long): Boolean {
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
            msg._when = `when`
            var p = messages
            if (p == null || `when` == 0L || `when` < p._when) {
                msg.next = p
                messages = msg
                condition.signalAll()
            } else {
                var prev: Message?
                while (true) {
                    prev = p
                    p = p?.next
                    if (p == null || `when` < p._when) {
                        break
                    }
                }
                msg.next = p
                prev!!.next = msg
                condition.signalAll()
            }
            return true
        }
    }

    internal fun hasMessages(h: Handler, what: Int, `object`: Any?): Boolean {
        lock.withLock {
            var p = messages
            while (p != null) {
                if (p.target === h && p.what == what && (`object` == null || p.obj == `object`)) {
                    return true
                }
                p = p.next
            }
            return false
        }
    }

    internal fun hasMessages(h: Handler, r: Runnable?, `object`: Any?): Boolean {
        lock.withLock {
            var p = messages
            while (p != null) {
                if (p.target === h && p._callback === r && (`object` == null || p.obj == `object`)) {
                    return true
                }
                p = p.next
            }
            return false
        }
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

    internal fun removeCallbacksAndMessages(h: Handler, r: Runnable?, `object`: Any?, matchRunnable: Boolean) {
        lock.withLock {
            var p = messages
            while (p != null && p.target === h &&
                (!matchRunnable || p._callback === r) &&
                (`object` == null || p.obj === `object`)
            ) {
                val n = p.next
                messages = n
                p.recycle()
                p = n
            }
            while (p != null) {
                val n = p.next
                if (n != null && n.target === h &&
                    (!matchRunnable || n._callback === r) &&
                    (`object` == null || n.obj === `object`)
                ) {
                    val nn = n.next
                    n.recycle()
                    p.next = nn
                    continue
                }
                p = n
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
                val now = SystemClock.uptimeMillis()
                var p = messages
                var prev: Message? = null
                while (p != null) {
                    if (p._when > now) break
                    prev = p
                    p = p.next
                }
                if (prev != null) {
                    prev.next = null
                } else {
                    messages = null
                }
                while (p != null) {
                    val next = p.next
                    p.recycle()
                    p = next
                }
            } else {
                var p = messages
                while (p != null) {
                    val next = p.next
                    p.recycle()
                    p = next
                }
                messages = null
            }
            condition.signalAll()
        }
    }

    fun interface IdleHandler {
        fun queueIdle(): Boolean
    }
}
