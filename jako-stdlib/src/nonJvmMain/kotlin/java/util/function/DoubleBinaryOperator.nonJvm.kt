package java.util.function

actual fun interface DoubleBinaryOperator {
    actual fun applyAsDouble(left: Double, right: Double): Double
}
