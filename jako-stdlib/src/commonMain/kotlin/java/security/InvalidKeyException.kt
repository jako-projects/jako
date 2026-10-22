@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

expect open class InvalidKeyException : KeyException {
    constructor()
    constructor(msg: String?)
    constructor(message: String?, cause: Throwable?)
    constructor(cause: Throwable?)
}
