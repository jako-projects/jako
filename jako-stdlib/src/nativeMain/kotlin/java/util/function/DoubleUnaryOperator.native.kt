package java.util.function

actual fun interface DoubleUnaryOperator {
    actual fun applyAsDouble(operand: Double): Double
}
