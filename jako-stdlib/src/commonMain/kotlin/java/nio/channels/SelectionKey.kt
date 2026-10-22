@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

/**
 * [Nullability Decision] attach/attachment use Any? because attachments are optional.
 */
expect abstract class SelectionKey {
    protected constructor()

    abstract fun channel(): SelectableChannel
    abstract fun selector(): Selector
    abstract fun isValid(): Boolean
    abstract fun cancel()
    abstract fun interestOps(): Int
    abstract fun interestOps(ops: Int): SelectionKey
    abstract fun readyOps(): Int
    fun isReadable(): Boolean
    fun isWritable(): Boolean
    fun isConnectable(): Boolean
    fun isAcceptable(): Boolean
    fun attach(ob: Any?): Any?
    fun attachment(): Any?

    companion object {
        val OP_READ: Int
        val OP_WRITE: Int
        val OP_CONNECT: Int
        val OP_ACCEPT: Int
    }
}
