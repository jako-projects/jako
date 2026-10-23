package java.lang.ref

import kotlin.concurrent.withLock

actual open class SoftReference<T> : Reference<T> {
    actual constructor(referent: T?) : super(referent, null)
    actual constructor(referent: T?, q: ReferenceQueue<in T>?) : super(referent, q)
    actual override fun get(): T? = super.get()
}
