package java.util.function

actual fun interface ToIntBiFunction<T, U> {
    actual fun applyAsInt(t: T, u: U): Int
}
