package java.util.function

actual fun interface Function<T, R> {
    actual fun apply(t: T): R
}
