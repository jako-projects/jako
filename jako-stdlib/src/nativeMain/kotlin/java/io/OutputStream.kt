package java.io

actual abstract class OutputStream actual constructor() : Closeable, Flushable {

    actual abstract fun write(b: Int)

    actual open fun write(b: ByteArray) {
        write(b, 0, b.size)
    }

    actual open fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        for (i in 0 until len) {
            write(b[off + i].toInt())
        }
    }

    actual override fun flush() {}

    actual override fun close() {}
}
