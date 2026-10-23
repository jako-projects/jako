@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

expect abstract class Buffer {
    open fun capacity(): Int
    open fun position(): Int
    open fun position(newPosition: Int): Buffer
    open fun limit(): Int
    open fun limit(newLimit: Int): Buffer
    open fun mark(): Buffer
    open fun reset(): Buffer
    open fun clear(): Buffer
    open fun flip(): Buffer
    open fun rewind(): Buffer
    open fun remaining(): Int
    open fun hasRemaining(): Boolean
    abstract fun isReadOnly(): Boolean
    abstract fun hasArray(): Boolean
    abstract fun array(): Any
    abstract fun arrayOffset(): Int
    abstract fun isDirect(): Boolean

    abstract val isReadOnly: Boolean
    abstract val isDirect: Boolean
}
