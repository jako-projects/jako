package java.lang.ref

import kotlin.concurrent.withLock

actual open class PhantomReference<T> actual constructor(
    referent: T?,
    q: ReferenceQueue<in T>?
) : Reference<T>(referent, q) {
    actual override fun get(): T? = null
}
