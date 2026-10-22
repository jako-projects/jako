package java.util.function

actual fun interface Consumer<T> {
    actual fun accept(t: T)
}
