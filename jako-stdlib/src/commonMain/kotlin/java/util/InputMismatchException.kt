@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class InputMismatchException : NoSuchElementException {
    constructor()
    constructor(s: String?)
}
