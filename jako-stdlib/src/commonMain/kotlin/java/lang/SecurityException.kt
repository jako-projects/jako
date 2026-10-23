@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect open class SecurityException : RuntimeException {
    constructor()
    constructor(s: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
