@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface DoubleBinaryOperator {
    fun applyAsDouble(left: Double, right: Double): Double
}
