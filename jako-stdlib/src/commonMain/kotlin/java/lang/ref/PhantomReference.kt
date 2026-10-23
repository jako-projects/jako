@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang.ref

expect open class PhantomReference<T> : Reference<T> {
    constructor(referent: T?, q: ReferenceQueue<in T>?)
    override fun get(): T?
}
