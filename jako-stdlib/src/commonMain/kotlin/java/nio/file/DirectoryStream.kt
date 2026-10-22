@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect interface DirectoryStream<T> : java.io.Closeable, Iterable<T> {
    fun interface Filter<T> {
        fun accept(entry: T): Boolean
    }
    override fun iterator(): MutableIterator<T>
}
