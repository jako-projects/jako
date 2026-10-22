@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

expect open class NotYetBoundException : IllegalStateException {
    constructor()
}
