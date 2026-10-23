@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class IllegalFormatFlagsException(f: String) : IllegalFormatException {
    open fun getFlags(): String
    open val flags: String
    override val message: String?
}
