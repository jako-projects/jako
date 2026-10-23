package java.util.function

actual fun interface DoubleToLongFunction {
    actual fun applyAsLong(value: Double): Long
}
