@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class IllegalFormatCodePointException(c: Int) : IllegalFormatException {
    open fun getCodePoint(): Int
    open val codePoint: Int
    override val message: String?
}
