@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent.locks

import java.lang.Thread

/**
 * [Nullability Decision] Java LockSupport.unpark(Thread), park(Object), and getBlocker(Thread)
 * accept/return nullable references.
 */
expect object LockSupport {
    fun unpark(thread: Thread?)
    fun park(blocker: Any?)
    fun parkNanos(blocker: Any?, nanos: Long)
    fun parkUntil(blocker: Any?, deadline: Long)
    fun getBlocker(t: Thread?): Any?
    fun park()
    fun parkNanos(nanos: Long)
    fun parkUntil(deadline: Long)
}
