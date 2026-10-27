package java.io

actual open class DataOutputStream actual constructor(
    out: OutputStream
) : FilterOutputStream(out), DataOutput {

    private var writtenCount: Int = 0

    actual fun size(): Int = writtenCount

    private fun inc(n: Int) {
        val temp = writtenCount + n
        if (temp < 0) {
            writtenCount = Int.MAX_VALUE
        } else {
            writtenCount = temp
        }
    }

    actual override fun write(b: Int) {
        out.write(b)
        inc(1)
    }

    actual override fun write(b: ByteArray, off: Int, len: Int) {
        out.write(b, off, len)
        inc(len)
    }

    actual override fun writeBoolean(v: Boolean) {
        write(if (v) 1 else 0)
    }

    actual override fun writeByte(v: Int) {
        write(v)
    }

    actual override fun writeShort(v: Int) {
        write((v ushr 8) and 0xFF)
        write((v ushr 0) and 0xFF)
    }

    actual override fun writeChar(v: Int) {
        write((v ushr 8) and 0xFF)
        write((v ushr 0) and 0xFF)
    }

    actual override fun writeInt(v: Int) {
        write((v ushr 24) and 0xFF)
        write((v ushr 16) and 0xFF)
        write((v ushr 8) and 0xFF)
        write((v ushr 0) and 0xFF)
    }

    actual override fun writeLong(v: Long) {
        write((v ushr 56).toInt() and 0xFF)
        write((v ushr 48).toInt() and 0xFF)
        write((v ushr 40).toInt() and 0xFF)
        write((v ushr 32).toInt() and 0xFF)
        write((v ushr 24).toInt() and 0xFF)
        write((v ushr 16).toInt() and 0xFF)
        write((v ushr 8).toInt() and 0xFF)
        write((v ushr 0).toInt() and 0xFF)
    }

    actual override fun writeFloat(v: Float) {
        writeInt(v.toRawBits())
    }

    actual override fun writeDouble(v: Double) {
        writeLong(v.toRawBits())
    }

    actual override fun writeBytes(s: String) {
        val len = s.length
        for (i in 0 until len) {
            write(s[i].code.toByte().toInt())
        }
    }

    actual override fun writeChars(s: String) {
        val len = s.length
        for (i in 0 until len) {
            val v = s[i].code
            write((v ushr 8) and 0xFF)
            write((v ushr 0) and 0xFF)
        }
    }

    actual override fun writeUTF(s: String) {
        val bytes = s.encodeToByteArray()
        if (bytes.size > 65535) {
            throw IOException("encoded string too long: ${bytes.size} bytes")
        }
        writeShort(bytes.size)
        write(bytes, 0, bytes.size)
    }

    actual override fun flush() {
        out.flush()
    }
}
