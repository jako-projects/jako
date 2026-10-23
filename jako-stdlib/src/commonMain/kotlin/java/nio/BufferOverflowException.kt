@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

/** [Nullability Decision] Java BufferOverflowException has only a non-null no-arg constructor. */
expect open class BufferOverflowException : RuntimeException {
    constructor()
}
