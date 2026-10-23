@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

/** [Nullability Decision] Java BufferUnderflowException has only a non-null no-arg constructor. */
expect open class BufferUnderflowException : RuntimeException {
    constructor()
}
