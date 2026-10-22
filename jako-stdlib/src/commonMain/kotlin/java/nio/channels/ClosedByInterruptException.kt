@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

expect open class ClosedByInterruptException : AsynchronousCloseException {
    constructor()
}
