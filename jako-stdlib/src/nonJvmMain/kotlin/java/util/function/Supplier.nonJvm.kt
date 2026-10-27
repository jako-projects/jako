package java.util.function

actual fun interface Supplier<T> {
    actual fun get(): T
}
