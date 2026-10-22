@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect open class InterruptedIOException : IOException {
    constructor()
    constructor(message: String?)
}
