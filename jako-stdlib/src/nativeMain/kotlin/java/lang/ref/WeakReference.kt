package java.lang.ref

import kotlin.concurrent.withLock
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

actual abstract class Reference<T>(
    internal var referent: T? = null,
    internal val queue: ReferenceQueue<in T>? = null
) {
    internal var enqueuedState: Int = 0 // 0 = never enqueued, 1 = currently in queue, 2 = removed from queue

    actual open fun get(): T? = referent

    actual open fun clear() {
        referent = null
    }

    actual open fun isEnqueued(): Boolean {
        val q = queue ?: return false
        return q.isReferenceEnqueued(this)
    }

    actual open fun enqueue(): Boolean {
        val q = queue ?: return false
        return q.enqueueReference(this)
    }
}

actual open class ReferenceQueue<T> actual constructor() {
    private val lock = ReentrantLock()
    private val notEmpty = lock.newCondition()
    private val queue = ArrayList<Reference<out T>>()

    internal fun isReferenceEnqueued(ref: Reference<*>): Boolean = lock.withLock {
        ref.enqueuedState == 1
    }

    @Suppress("UNCHECKED_CAST")
    internal fun enqueueReference(ref: Reference<*>): Boolean = lock.withLock {
        if (ref.enqueuedState != 0) return@withLock false
        ref.enqueuedState = 1
        queue.add(ref as Reference<out T>)
        notEmpty.signalAll()
        true
    }

    actual open fun poll(): Reference<out T>? = lock.withLock {
        if (queue.isEmpty()) return@withLock null
        val ref = queue.removeAt(0)
        ref.enqueuedState = 2
        ref
    }

    actual open fun remove(timeout: Long): Reference<out T>? {
        if (timeout < 0L) throw IllegalArgumentException("Negative timeout value")
        return lock.withLock {
            if (queue.isNotEmpty()) {
                val ref = queue.removeAt(0)
                ref.enqueuedState = 2
                return@withLock ref
            }
            if (timeout == 0L) {
                while (queue.isEmpty()) {
                    notEmpty.await()
                }
            } else {
                var nanos = TimeUnit.MILLISECONDS.toNanos(timeout)
                while (queue.isEmpty() && nanos > 0L) {
                    nanos = notEmpty.awaitNanos(nanos)
                }
                if (queue.isEmpty()) return@withLock null
            }
            val ref = queue.removeAt(0)
            ref.enqueuedState = 2
            ref
        }
    }

    actual open fun remove(): Reference<out T>? = remove(0L)
}

actual open class WeakReference<T> : Reference<T> {
    actual constructor(referent: T?) : super(referent, null)
    actual constructor(referent: T?, q: ReferenceQueue<in T>?) : super(referent, q)
}

actual open class SoftReference<T> : Reference<T> {
    actual constructor(referent: T?) : super(referent, null)
    actual constructor(referent: T?, q: ReferenceQueue<in T>?) : super(referent, q)
    actual override fun get(): T? = super.get()
}

actual open class PhantomReference<T> actual constructor(
    referent: T?,
    q: ReferenceQueue<in T>?
) : Reference<T>(referent, q) {
    actual override fun get(): T? = null
}
