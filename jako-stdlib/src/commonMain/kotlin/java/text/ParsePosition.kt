@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

expect class ParsePosition {
    constructor()
    constructor(index: Int)
    fun getIndex(): Int
    fun setIndex(index: Int)
    fun getErrorIndex(): Int
    fun setErrorIndex(ei: Int)
    var index: Int
    var errorIndex: Int
}
