package java.util.function

actual fun interface ToIntFunction<T> {
    actual fun applyAsInt(value: T): Int
}
