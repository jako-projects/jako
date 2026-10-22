@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

/**
 * [Nullability Decision] keyFor returns SelectionKey? (null if not registered with the selector);
 * register attachment is Any?.
 */
expect abstract class SelectableChannel : Channel, InterruptibleChannel {
    protected constructor()

    abstract fun validOps(): Int
    open fun isRegistered(): Boolean
    open fun keyFor(sel: Selector): SelectionKey?
    open fun register(sel: Selector, ops: Int, att: Any?): SelectionKey
    fun register(sel: Selector, ops: Int): SelectionKey
    open fun configureBlocking(block: Boolean): SelectableChannel
    open fun isBlocking(): Boolean
    open fun blockingLock(): Any
}
