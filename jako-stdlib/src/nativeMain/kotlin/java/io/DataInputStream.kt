package java.io

actual open class DataInputStream actual constructor(
    inStream: InputStream
) : FilterInputStream(inStream), DataInput {

    actual override fun read(b: ByteArray): Int = inStream.read(b)

    actual override fun read(b: ByteArray, off: Int, len: Int): Int = inStream.read(b, off, len)

    actual override fun readFully(b: ByteArray) {
        readFully(b, 0, b.size)
    }

    actual override fun readFully(b: ByteArray, off: Int, len: Int) {
        if (len < 0) throw IndexOutOfBoundsException()
        var n = 0
        while (n < len) {
            val count = inStream.read(b, off + n, len - n)
            if (count < 0) throw EOFException()
            n += count
        }
    }

    actual override fun skipBytes(n: Int): Int {
        var total = 0
        var cur = 0L
        while (total < n && inStream.skip((n - total).toLong()).also { cur = it } > 0) {
            total += cur.toInt()
        }
        return total
    }

    actual override fun readBoolean(): Boolean {
        val ch = inStream.read()
        if (ch < 0) throw EOFException()
        return ch != 0
    }

    actual override fun readByte(): Byte {
        val ch = inStream.read()
        if (ch < 0) throw EOFException()
        return ch.toByte()
    }

    actual override fun readUnsignedByte(): Int {
        val ch = inStream.read()
        if (ch < 0) throw EOFException()
        return ch
    }

    actual override fun readShort(): Short {
        val ch1 = inStream.read()
        val ch2 = inStream.read()
        if ((ch1 or ch2) < 0) throw EOFException()
        return ((ch1 shl 8) + (ch2 shl 0)).toShort()
    }

    actual override fun readUnsignedShort(): Int {
        val ch1 = inStream.read()
        val ch2 = inStream.read()
        if ((ch1 or ch2) < 0) throw EOFException()
        return (ch1 shl 8) + (ch2 shl 0)
    }

    actual override fun readChar(): Char {
        val ch1 = inStream.read()
        val ch2 = inStream.read()
        if ((ch1 or ch2) < 0) throw EOFException()
        return ((ch1 shl 8) + (ch2 shl 0)).toChar()
    }

    actual override fun readInt(): Int {
        val ch1 = inStream.read()
        val ch2 = inStream.read()
        val ch3 = inStream.read()
        val ch4 = inStream.read()
        if ((ch1 or ch2 or ch3 or ch4) < 0) throw EOFException()
        return (ch1 shl 24) or (ch2 shl 16) or (ch3 shl 8) or ch4
    }

    actual override fun readLong(): Long {
        val b = ByteArray(8)
        readFully(b, 0, 8)
        return ((b[0].toLong() and 0xFF) shl 56) or
                ((b[1].toLong() and 0xFF) shl 48) or
                ((b[2].toLong() and 0xFF) shl 40) or
                ((b[3].toLong() and 0xFF) shl 32) or
                ((b[4].toLong() and 0xFF) shl 24) or
                ((b[5].toLong() and 0xFF) shl 16) or
                ((b[6].toLong() and 0xFF) shl 8) or
                (b[7].toLong() and 0xFF)
    }

    actual override fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    actual override fun readDouble(): Double {
        return Double.fromBits(readLong())
    }

    actual override fun readUTF(): String {
        val utflen = readUnsignedShort()
        val bytearr = ByteArray(utflen)
        readFully(bytearr, 0, utflen)
        return bytearr.decodeToString()
    }
}

