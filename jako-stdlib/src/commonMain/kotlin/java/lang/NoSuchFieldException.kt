@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect open class NoSuchFieldException : Exception {
    constructor()
    constructor(s: String?)
}
