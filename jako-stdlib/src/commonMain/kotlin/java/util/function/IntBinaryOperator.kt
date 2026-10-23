@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface IntBinaryOperator {
    fun applyAsInt(left: Int, right: Int): Int
}
