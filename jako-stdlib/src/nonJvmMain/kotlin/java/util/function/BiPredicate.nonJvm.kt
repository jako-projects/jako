package java.util.function

actual fun interface BiPredicate<T, U> {
    actual fun test(t: T, u: U): Boolean
}
