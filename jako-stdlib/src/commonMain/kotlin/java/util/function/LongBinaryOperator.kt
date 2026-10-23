@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface LongBinaryOperator {
    fun applyAsLong(left: Long, right: Long): Long
}
