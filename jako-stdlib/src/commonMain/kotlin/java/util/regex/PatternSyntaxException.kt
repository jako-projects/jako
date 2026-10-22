@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.regex

/**
 * [Nullability Decision] desc and regex may be null when constructed manually; message is non-null formatted string.
 */
expect open class PatternSyntaxException(
    desc: String?,
    regex: String?,
    index: Int
) : IllegalArgumentException {
    open fun getIndex(): Int
    open val index: Int
    open fun getDescription(): String?
    open val description: String?
    open fun getPattern(): String?
    open val pattern: String?
    override val message: String?
}
