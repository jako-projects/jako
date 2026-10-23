@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.io.Closeable

expect abstract class Selector : Closeable {
    protected constructor()

    abstract fun isOpen(): Boolean
    abstract fun keys(): MutableSet<SelectionKey>
    abstract fun selectedKeys(): MutableSet<SelectionKey>
    abstract fun selectNow(): Int
    abstract fun select(timeout: Long): Int
    abstract fun select(): Int
    abstract fun wakeup(): Selector
    abstract override fun close()

    companion object {
        fun open(): Selector
    }
}
