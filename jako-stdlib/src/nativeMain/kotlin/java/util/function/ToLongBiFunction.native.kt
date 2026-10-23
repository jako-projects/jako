package java.util.function

actual fun interface ToLongBiFunction<T, U> {
    actual fun applyAsLong(t: T, u: U): Long
}
