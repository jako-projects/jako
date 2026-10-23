package java.lang.ref

import kotlin.concurrent.withLock

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
