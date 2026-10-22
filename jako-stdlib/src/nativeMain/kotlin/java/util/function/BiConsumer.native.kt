package java.util.function

actual fun interface BiConsumer<T, U> {
    actual fun accept(t: T, u: U)
}
