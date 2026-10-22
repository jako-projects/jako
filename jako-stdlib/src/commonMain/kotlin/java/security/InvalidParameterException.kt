@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

expect open class InvalidParameterException : IllegalArgumentException {
    constructor()
    constructor(msg: String?)
}
