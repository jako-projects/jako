package java.util.function

actual fun interface LongToDoubleFunction {
    actual fun applyAsDouble(value: Long): Double
}
