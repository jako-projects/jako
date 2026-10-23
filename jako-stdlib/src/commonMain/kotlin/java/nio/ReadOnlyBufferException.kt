@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio

/** [Nullability Decision] Java ReadOnlyBufferException has only a non-null no-arg constructor. */
expect open class ReadOnlyBufferException : UnsupportedOperationException {
    constructor()
}
