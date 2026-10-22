package java.nio.file

actual abstract class FileSystem : java.io.Closeable {
    actual abstract fun getPath(first: String, vararg more: String): Path
    actual abstract fun getPathMatcher(syntaxAndPattern: String): PathMatcher
    actual abstract fun getSeparator(): String
    actual abstract fun isOpen(): Boolean
    actual abstract override fun close()

    actual val separator: String get() = getSeparator()
    actual val isOpen: Boolean get() = isOpen()
}
