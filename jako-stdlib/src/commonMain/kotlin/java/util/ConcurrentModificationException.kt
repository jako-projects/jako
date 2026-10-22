@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class ConcurrentModificationException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(cause: Throwable?)
    constructor(message: String?, cause: Throwable?)
}
