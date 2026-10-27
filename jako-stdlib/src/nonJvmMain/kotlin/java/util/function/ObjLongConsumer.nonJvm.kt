package java.util.function

actual fun interface ObjLongConsumer<T> {
    actual fun accept(t: T, value: Long)
}
