package java.util.function

actual fun interface ObjIntConsumer<T> {
    actual fun accept(t: T, value: Int)
}
