package java.io

actual interface Flushable {
    @Throws(IOException::class)
    actual fun flush()
}
