package java.util.function

actual fun interface ToLongFunction<T> {
    actual fun applyAsLong(value: T): Long
}
