package java.io

actual open class FilterInputStream protected actual constructor(
    protected open val inStream: InputStream
) : InputStream() {
    protected open val `in`: InputStream get() = inStream

    actual override fun read(): Int = inStream.read()
    actual override fun read(b: ByteArray): Int = read(b, 0, b.size)
    actual override fun read(b: ByteArray, off: Int, len: Int): Int = inStream.read(b, off, len)
    actual override fun skip(n: Long): Long = inStream.skip(n)
    actual override fun available(): Int = inStream.available()
    actual override fun close() = inStream.close()
    actual override fun mark(readlimit: Int) = inStream.mark(readlimit)
    actual override fun reset() = inStream.reset()
    actual override fun markSupported(): Boolean = inStream.markSupported()
}
