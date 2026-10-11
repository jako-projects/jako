package java.io

actual open class FilterReader protected actual constructor(
    protected open val inReader: Reader
) : Reader(inReader) {
    protected open val `in`: Reader get() = inReader

    actual override fun read(): Int = inReader.read()
    actual override fun read(cbuf: CharArray): Int = inReader.read(cbuf)
    actual override fun read(cbuf: CharArray, off: Int, len: Int): Int = inReader.read(cbuf, off, len)
    actual override fun skip(n: Long): Long = inReader.skip(n)
    override fun ready(): Boolean = inReader.ready()
    actual override fun markSupported(): Boolean = inReader.markSupported()
    actual override fun mark(readAheadLimit: Int) = inReader.mark(readAheadLimit)
    actual override fun reset() = inReader.reset()
    actual override fun close() = inReader.close()
}
