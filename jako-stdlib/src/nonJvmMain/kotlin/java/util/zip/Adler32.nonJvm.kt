package java.util.zip

actual class Adler32 actual constructor() : Checksum {
    private var s1 = 1L
    private var s2 = 0L

    actual override fun update(b: Int) {
        s1 = (s1 + (b and 0xFF)) % BASE
        s2 = (s2 + s1) % BASE
    }

    actual override fun update(b: ByteArray, off: Int, len: Int) {
        for (i in off until off + len) {
            s1 = (s1 + (b[i].toInt() and 0xFF)) % BASE
            s2 = (s2 + s1) % BASE
        }
    }

    actual fun update(b: ByteArray) {
        update(b, 0, b.size)
    }

    actual override fun getValue(): Long {
        return (s2 shl 16) or s1
    }

    actual override val value: Long get() = getValue()

    actual override fun reset() {
        s1 = 1L
        s2 = 0L
    }

    companion object {
        private const val BASE = 65521L
    }
}
