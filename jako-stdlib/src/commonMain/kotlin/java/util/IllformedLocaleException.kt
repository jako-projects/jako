@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class IllformedLocaleException : RuntimeException {
    constructor()
    constructor(message: String?)
    constructor(message: String?, errorIndex: Int)

    open fun getErrorIndex(): Int
    open val errorIndex: Int
}
