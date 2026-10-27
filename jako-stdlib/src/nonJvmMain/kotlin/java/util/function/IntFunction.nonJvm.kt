package java.util.function

actual fun interface IntFunction<R> {
    actual fun apply(value: Int): R
}
