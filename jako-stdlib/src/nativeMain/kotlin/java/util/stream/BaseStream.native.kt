package java.util.stream

import java.lang.Runnable
import java.util.Spliterator

actual interface BaseStream<T, S : BaseStream<T, S>> : AutoCloseable {
    actual fun iterator(): MutableIterator<T>
    actual fun spliterator(): Spliterator<T>
    actual fun isParallel(): Boolean
    actual fun sequential(): S
    actual fun parallel(): S
    actual fun unordered(): S
    actual fun onClose(closeHandler: Runnable): S
    actual override fun close()
}
