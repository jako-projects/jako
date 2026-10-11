package java.lang.ref

actual abstract class Reference<T>(internal var referent: T? = null) {
    actual open fun get(): T? = referent
    actual open fun clear() { referent = null }
}

actual open class WeakReference<T> actual constructor(referent: T?) : Reference<T>(referent)

