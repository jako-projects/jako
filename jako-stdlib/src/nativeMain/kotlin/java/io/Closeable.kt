package java.io

actual interface Closeable : AutoCloseable {
    actual override fun close()
}
