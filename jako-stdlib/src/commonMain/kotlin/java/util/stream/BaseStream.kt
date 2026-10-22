@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.stream

import java.lang.Runnable
import java.util.Spliterator

expect interface BaseStream<T, S : BaseStream<T, S>> : AutoCloseable {
    fun iterator(): MutableIterator<T>
    fun spliterator(): Spliterator<T>
    fun isParallel(): Boolean
    fun sequential(): S
    fun parallel(): S
    fun unordered(): S
    fun onClose(closeHandler: Runnable): S
    override fun close()
}
