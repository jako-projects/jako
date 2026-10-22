@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.atomic

import java.io.Serializable

expect class AtomicBoolean(initialValue: Boolean) : Serializable {
    constructor()
    fun get(): Boolean
    fun set(newValue: Boolean)
    fun lazySet(newValue: Boolean)
    fun getAndSet(newValue: Boolean): Boolean
    fun compareAndSet(expect: Boolean, update: Boolean): Boolean
    fun weakCompareAndSet(expect: Boolean, update: Boolean): Boolean
}
