package java.util.zip

actual class CRC32 actual constructor() : Checksum {
    private var crc = -1

    actual override fun update(b: Int) {
        crc = CRC_TABLE[(crc xor (b and 0xFF)) and 0xFF] xor (crc ushr 8)
    }

    actual override fun update(b: ByteArray, off: Int, len: Int) {
        for (i in off until off + len) {
            crc = CRC_TABLE[(crc xor (b[i].toInt() and 0xFF)) and 0xFF] xor (crc ushr 8)
        }
    }

    actual fun update(b: ByteArray) {
        update(b, 0, b.size)
    }

    actual override fun getValue(): Long {
        return crc.toLong().inv() and 0xFFFFFFFFL
    }

    actual override val value: Long get() = getValue()

    actual override fun reset() {
        crc = -1
    }

    companion object {
        private val CRC_TABLE = IntArray(256) { i ->
            var c = i
            for (j in 0 until 8) {
                c = if ((c and 1) != 0) 0xEDB88320.toInt() xor (c ushr 1) else c ushr 1
            }
            c
        }
    }
}
