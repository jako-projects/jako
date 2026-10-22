@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

import java.io.Closeable

expect abstract class FileSystem : Closeable {
    abstract fun getPath(first: String, vararg more: String): Path
    abstract fun getPathMatcher(syntaxAndPattern: String): PathMatcher
    abstract fun getSeparator(): String
    abstract fun isOpen(): Boolean
    abstract override fun close()

    val separator: String
    val isOpen: Boolean
}
