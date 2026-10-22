package java.util.function

actual fun interface ObjDoubleConsumer<T> {
    actual fun accept(t: T, value: Double)
}
