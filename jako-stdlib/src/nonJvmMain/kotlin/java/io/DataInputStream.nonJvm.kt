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

    actual override fun readLine(): String? {
        val sb = StringBuilder()
        var c = inStream.read()
        if (c == -1) return null
        while (c != -1 && c != '\n'.code) {
            if (c == '\r'.code) {
                val c2 = inStream.read()
                if (c2 != '\n'.code && c2 != -1) {
                    val stream = inStream
                    if (stream is PushbackInputStream) {
                        stream.unread(c2)
                    }
                }
                break
            }
            sb.append((c and 0xFF).toChar())
            c = inStream.read()
        }
        return sb.toString()
    }

    actual override fun readUTF(): String {
        val utflen = readUnsignedShort()
        val bytearr = ByteArray(utflen)
        readFully(bytearr, 0, utflen)
        return ModifiedUtf8.decode(bytearr, 0, utflen)
    }
}

internal object ModifiedUtf8 {
    fun encodedLength(s: String): Long {
        var utflen = 0L
        for (i in 0 until s.length) {
            val c = s[i].code
            utflen += when {
                c in 0x0001..0x007F -> 1L
                c > 0x07FF -> 3L
                else -> 2L
            }
        }
        return utflen
    }

    fun encode(s: String): ByteArray {
        val utflenLong = encodedLength(s)
        if (utflenLong > 65535L) {
            throw UTFDataFormatException("encoded string too long: $utflenLong bytes")
        }
        return encodeUnchecked(s, utflenLong.toInt())
    }

    fun encodeUnchecked(s: String, utflen: Int = encodedLength(s).toInt()): ByteArray {
        val bytearr = ByteArray(utflen)
        var count = 0
        for (i in 0 until s.length) {
            val c = s[i].code
            when {
                c in 0x0001..0x007F -> {
                    bytearr[count++] = c.toByte()
                }
                c > 0x07FF -> {
                    bytearr[count++] = (0xE0 or ((c shr 12) and 0x0F)).toByte()
                    bytearr[count++] = (0x80 or ((c shr 6) and 0x3F)).toByte()
                    bytearr[count++] = (0x80 or (c and 0x3F)).toByte()
                }
                else -> {
                    bytearr[count++] = (0xC0 or ((c shr 6) and 0x1F)).toByte()
                    bytearr[count++] = (0x80 or (c and 0x3F)).toByte()
                }
            }
        }
        return bytearr
    }

    fun decode(bytearr: ByteArray, offset: Int = 0, utflen: Int = bytearr.size - offset): String {
        val end = offset + utflen
        val chararr = CharArray(utflen)
        var count = offset
        var charCount = 0
        while (count < end) {
            val c = bytearr[count].toInt() and 0xff
            if (c > 127) break
            count++
            chararr[charCount++] = c.toChar()
        }
        while (count < end) {
            val c = bytearr[count].toInt() and 0xff
            when (c ushr 4) {
                in 0..7 -> {
                    count++
                    chararr[charCount++] = c.toChar()
                }
                12, 13 -> {
                    count += 2
                    if (count > end) {
                        throw UTFDataFormatException("malformed input: partial character at end")
                    }
                    val char2 = bytearr[count - 1].toInt()
                    if ((char2 and 0xC0) != 0x80) {
                        throw UTFDataFormatException("malformed input around byte ${count - offset}")
                    }
                    chararr[charCount++] = (((c and 0x1F) shl 6) or (char2 and 0x3F)).toChar()
                }
                14 -> {
                    count += 3
                    if (count > end) {
                        throw UTFDataFormatException("malformed input: partial character at end")
                    }
                    val char2 = bytearr[count - 2].toInt()
                    val char3 = bytearr[count - 1].toInt()
                    if ((char2 and 0xC0) != 0x80 || (char3 and 0xC0) != 0x80) {
                        throw UTFDataFormatException("malformed input around byte ${count - offset - 1}")
                    }
                    chararr[charCount++] = (
                        ((c and 0x0F) shl 12) or
                            ((char2 and 0x3F) shl 6) or
                            (char3 and 0x3F)
                        ).toChar()
                }
                else -> throw UTFDataFormatException("malformed input around byte ${count - offset}")
            }
        }
        return chararr.concatToString(0, charCount)
    }
}
