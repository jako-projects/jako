package java.io

import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CodingErrorAction

actual open class InputStreamReader : Reader {
    private val inStream: InputStream
    private val encodingName: String
    private val decoder: CharsetDecoder
    private val byteBuf = ByteArray(8192)
    private var byteBufPos = 0
    private var byteBufLimit = 0
    private var eof = false

    actual constructor(inStream: InputStream) : this(inStream, "UTF-8")

    actual constructor(inStream: InputStream, charsetName: String) : super() {
        this.inStream = inStream
        this.encodingName = charsetName
        this.decoder = Charset.forName(charsetName).newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
    }

    actual constructor(inStream: InputStream, cs: Charset) : super() {
        this.inStream = inStream
        this.encodingName = cs.name()
        this.decoder = cs.newDecoder()
            .onMalformedInput(CodingErrorAction.REPLACE)
            .onUnmappableCharacter(CodingErrorAction.REPLACE)
    }

    fun getEncoding(): String? = encodingName

    actual override fun read(): Int {
        val cb = CharArray(1)
        return if (read(cb, 0, 1) == -1) -1 else cb[0].code
    }

    actual override fun read(cbuf: CharArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > cbuf.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, cbuf.size=${cbuf.size}")
        }
        if (len == 0) return 0

        val out = CharBuffer.wrap(cbuf, off, len)

        while (out.hasRemaining()) {
            if (byteBufPos >= byteBufLimit) {
                if (eof) break
                // If some characters were already read and stream doesn't have bytes immediately available, return
                if (out.position() > off && inStream.available() <= 0) break

                byteBufPos = 0
                byteBufLimit = 0
                val readBytes = inStream.read(byteBuf, 0, byteBuf.size)
                if (readBytes <= 0) {
                    eof = true
                } else {
                    byteBufLimit = readBytes
                }
            }

            val bb = ByteBuffer.wrap(byteBuf, byteBufPos, byteBufLimit - byteBufPos)
            val cr = decoder.decode(bb, out, eof)
            byteBufPos = bb.position()

            if (cr.isUnderflow) {
                if (eof) {
                    decoder.flush(out)
                    break
                }
                // Underflow with partial multi-byte sequence remaining in byteBuf:
                // compact remaining bytes to front and read more from inStream
                if (byteBufPos < byteBufLimit) {
                    val rem = byteBufLimit - byteBufPos
                    byteBuf.copyInto(byteBuf, 0, byteBufPos, byteBufLimit)
                    byteBufPos = 0
                    byteBufLimit = rem
                    val readBytes = inStream.read(byteBuf, byteBufLimit, byteBuf.size - byteBufLimit)
                    if (readBytes <= 0) {
                        eof = true
                    } else {
                        byteBufLimit += readBytes
                    }
                }
                continue
            }
            if (cr.isOverflow) {
                break
            }
            if (cr.isError) {
                cr.throwException()
            }
        }

        val charsRead = out.position() - off
        return if (charsRead == 0 && eof) -1 else charsRead
    }

    override fun ready(): Boolean = (byteBufLimit - byteBufPos) > 0 || inStream.available() > 0

    actual override fun close() = inStream.close()
}
