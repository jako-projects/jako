package java.util.function

actual fun interface ToDoubleFunction<T> {
    actual fun applyAsDouble(value: T): Double
}
