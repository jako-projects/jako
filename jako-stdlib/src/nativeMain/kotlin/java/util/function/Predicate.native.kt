package java.util.function

actual fun interface Predicate<T> {
    actual fun test(t: T): Boolean
}
