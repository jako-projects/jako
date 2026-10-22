@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class NoSuchElementException : RuntimeException {
    constructor()
    constructor(message: String?)
}
