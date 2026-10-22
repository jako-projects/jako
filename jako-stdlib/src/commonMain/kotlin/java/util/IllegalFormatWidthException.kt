@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class IllegalFormatWidthException(w: Int) : IllegalFormatException {
    open fun getWidth(): Int
    open val width: Int
    override val message: String?
}
