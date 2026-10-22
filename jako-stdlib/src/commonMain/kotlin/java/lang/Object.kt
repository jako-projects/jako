@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import kotlin.Long
import kotlin.Int

expect open class Object {
    fun wait()
    fun wait(timeoutMillis: Long)
    fun wait(timeoutMillis: Long, nanos: Int)
    fun notify()
    fun notifyAll()
}
