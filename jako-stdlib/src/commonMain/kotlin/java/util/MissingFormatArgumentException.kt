@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util

expect open class MissingFormatArgumentException(s: String) : IllegalFormatException {
    open fun getFormatSpecifier(): String
    open val formatSpecifier: String
    override val message: String?
}
