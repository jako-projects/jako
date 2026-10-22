@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.net

expect open class URISyntaxException : Exception {
    constructor(input: String, reason: String, index: Int)
    constructor(input: String, reason: String)
    open fun getInput(): String
    open fun getReason(): String
    open fun getIndex(): Int
    open val input: String
    open val reason: String
    open val index: Int
}
