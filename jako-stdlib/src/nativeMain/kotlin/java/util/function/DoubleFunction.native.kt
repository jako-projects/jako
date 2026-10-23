package java.util.function

actual fun interface DoubleFunction<R> {
    actual fun apply(value: Double): R
}
