package java.util.function

actual fun interface IntToDoubleFunction {
    actual fun applyAsDouble(value: Int): Double
}
