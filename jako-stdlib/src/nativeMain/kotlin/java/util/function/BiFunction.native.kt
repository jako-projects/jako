package java.util.function

actual fun interface BiFunction<T, U, R> {
    actual fun apply(t: T, u: U): R
}
