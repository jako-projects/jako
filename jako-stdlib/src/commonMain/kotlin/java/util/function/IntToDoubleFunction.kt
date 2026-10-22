@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.util.function

expect fun interface IntToDoubleFunction {
    fun applyAsDouble(value: Int): Double
}
