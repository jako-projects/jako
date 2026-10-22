@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang.ref

expect open class WeakReference<T> : Reference<T> {
    constructor(referent: T?)
    constructor(referent: T?, q: ReferenceQueue<in T>?)
}
