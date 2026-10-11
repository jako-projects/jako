package java.nio.file

import java.lang.System

actual abstract class FileSystem : java.io.Closeable {
    actual abstract fun getPath(first: String, vararg more: String): Path
    actual abstract fun getSeparator(): String
    actual abstract fun isOpen(): Boolean
    actual abstract override fun close()

    actual val separator: String get() = getSeparator()
    actual val isOpen: Boolean get() = isOpen()
}

private object DefaultFileSystem : FileSystem() {
    override fun getPath(first: String, vararg more: String): Path = Paths.get(first, *more)

    override fun getSeparator(): String = System.getProperty("file.separator", "/")

    override fun isOpen(): Boolean = true

    override fun close() {
        throw UnsupportedOperationException()
    }
}

actual class FileSystems {
    actual companion object {
        actual fun getDefault(): FileSystem = DefaultFileSystem
    }
}
