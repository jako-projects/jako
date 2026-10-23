@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

expect open class CoderResult {
    fun isUnderflow(): Boolean
    fun isOverflow(): Boolean
    fun isError(): Boolean
    fun isMalformed(): Boolean
    fun isUnmappable(): Boolean
    fun length(): Int
    fun throwException()

    val isUnderflow: Boolean
    val isOverflow: Boolean
    val isError: Boolean
    val isMalformed: Boolean
    val isUnmappable: Boolean

    companion object {
        val UNDERFLOW: CoderResult
        val OVERFLOW: CoderResult
        fun malformedForLength(length: Int): CoderResult
        fun unmappableForLength(length: Int): CoderResult
    }
}
