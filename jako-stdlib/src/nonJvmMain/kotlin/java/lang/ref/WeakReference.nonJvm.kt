package java.lang.ref

import kotlin.concurrent.withLock

actual open class WeakReference<T> : Reference<T> {
    actual constructor(referent: T?) : super(referent, null)
    actual constructor(referent: T?, q: ReferenceQueue<in T>?) : super(referent, q)
}
