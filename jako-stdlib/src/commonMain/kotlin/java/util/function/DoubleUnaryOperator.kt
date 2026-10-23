@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface DoubleUnaryOperator {
    fun applyAsDouble(operand: Double): Double
}
