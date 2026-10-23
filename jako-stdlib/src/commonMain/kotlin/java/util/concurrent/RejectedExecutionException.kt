@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.concurrent

expect open class RejectedExecutionException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
