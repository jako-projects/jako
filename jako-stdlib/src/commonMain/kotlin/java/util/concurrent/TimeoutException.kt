@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect open class TimeoutException : Exception {
    constructor()
    constructor(message: String?)
}
