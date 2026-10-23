@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect open class CancellationException : IllegalStateException {
    constructor()
    constructor(message: String?)
}
