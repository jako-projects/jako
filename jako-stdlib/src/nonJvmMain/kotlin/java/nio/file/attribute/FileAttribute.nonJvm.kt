package java.nio.file.attribute

actual interface FileAttribute<T> {
    actual fun name(): String
    actual fun value(): T
}
