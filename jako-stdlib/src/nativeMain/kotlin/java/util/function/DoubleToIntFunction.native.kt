package java.util.function

actual fun interface DoubleToIntFunction {
    actual fun applyAsInt(value: Double): Int
}
