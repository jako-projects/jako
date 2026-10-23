@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang.ref

expect open class SoftReference<T> : Reference<T> {
    constructor(referent: T?)
    constructor(referent: T?, q: ReferenceQueue<in T>?)
    override fun get(): T?
}
