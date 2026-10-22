@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface IntUnaryOperator {
    fun applyAsInt(operand: Int): Int
}
