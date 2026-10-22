@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect open class ExecutionException : Exception {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
