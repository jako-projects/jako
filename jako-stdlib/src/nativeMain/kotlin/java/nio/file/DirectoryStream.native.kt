package java.nio.file

import java.nio.file.attribute.nativeReadBasicAttributes

actual interface DirectoryStream<T> : java.io.Closeable, Iterable<T> {
    actual fun interface Filter<T> {
        actual fun accept(entry: T): Boolean
    }
    actual override fun iterator(): MutableIterator<T>
}
