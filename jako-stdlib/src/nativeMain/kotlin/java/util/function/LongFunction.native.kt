package java.util.function

actual fun interface LongFunction<R> {
    actual fun apply(value: Long): R
}
