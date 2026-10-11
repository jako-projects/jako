@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

expect open class ParseException : Exception {
    constructor(s: String?, errorOffset: Int)
    val errorOffset: Int
}
