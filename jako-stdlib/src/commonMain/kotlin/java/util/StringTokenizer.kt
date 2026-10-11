@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class StringTokenizer : Enumeration<Any> {
    constructor(str: String, delim: String, returnDelims: Boolean)
    constructor(str: String, delim: String)
    constructor(str: String)

    open fun hasMoreTokens(): Boolean
    open fun nextToken(): String
    open fun nextToken(delim: String): String
    open fun countTokens(): Int

    override fun hasMoreElements(): Boolean
    override fun nextElement(): Any
}
