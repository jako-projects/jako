@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

/** [Nullability Decision] Java NonReadableChannelException has only a non-null no-arg constructor. */
expect open class NonReadableChannelException : IllegalStateException {
    constructor()
}
