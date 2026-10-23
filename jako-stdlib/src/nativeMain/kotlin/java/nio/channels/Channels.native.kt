package java.nio.channels

import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder
import kotlin.concurrent.withLock

actual class Channels private constructor() {
    actual companion object {
        actual fun newInputStream(ch: ReadableByteChannel): InputStream {
            return object : InputStream() {
                override fun read(): Int {
                    val b = ByteArray(1)
                    val n = read(b, 0, 1)
                    return if (n == 1) (b[0].toInt() and 0xFF) else -1
                }

                override fun read(b: ByteArray, off: Int, len: Int): Int {
                    if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
                    if (len == 0) return 0
                    val bb = ByteBuffer.wrap(b, off, len)
                    return ch.read(bb)
                }

                override fun close() {
                    ch.close()
                }
            }
        }

        actual fun newOutputStream(ch: WritableByteChannel): OutputStream {
            return object : OutputStream() {
                override fun write(b: Int) {
                    write(byteArrayOf(b.toByte()), 0, 1)
                }

                override fun write(b: ByteArray, off: Int, len: Int) {
                    if (off < 0 || len < 0 || off + len > b.size) throw IndexOutOfBoundsException()
                    if (len == 0) return
                    val bb = ByteBuffer.wrap(b, off, len)
                    while (bb.hasRemaining()) {
                        ch.write(bb)
                    }
                }

                override fun close() {
                    ch.close()
                }
            }
        }

        actual fun newChannel(inStream: InputStream): ReadableByteChannel {
            return object : ReadableByteChannel {
                private var openFlag = true
                override val isOpen: Boolean get() = openFlag
                override fun isOpen(): Boolean = openFlag

                override fun read(dst: ByteBuffer): Int {
                    if (!openFlag) throw ClosedChannelException()
                    if (dst.isReadOnly()) throw ReadOnlyBufferException()
                    val rem = dst.remaining()
                    if (rem == 0) return 0
                    val pos = dst.position()
                    val n = inStream.read(dst.arrayData, dst.offset + pos, rem)
                    if (n < 0) return -1
                    dst.position(pos + n)
                    return n
                }

                override fun close() {
                    if (!openFlag) return
                    openFlag = false
                    inStream.close()
                }
            }
        }

        actual fun newChannel(out: OutputStream): WritableByteChannel {
            return object : WritableByteChannel {
                private var openFlag = true
                override val isOpen: Boolean get() = openFlag
                override fun isOpen(): Boolean = openFlag

                override fun write(src: ByteBuffer): Int {
                    if (!openFlag) throw ClosedChannelException()
                    val rem = src.remaining()
                    if (rem == 0) return 0
                    val pos = src.position()
                    out.write(src.arrayData, src.offset + pos, rem)
                    src.position(pos + rem)
                    return rem
                }

                override fun close() {
                    if (!openFlag) return
                    openFlag = false
                    out.close()
                }
            }
        }

        actual fun newReader(ch: ReadableByteChannel, dec: CharsetDecoder, minBufferCap: Int): Reader {
            return InputStreamReader(newInputStream(ch), dec.charset())
        }

        actual fun newReader(ch: ReadableByteChannel, csName: String): Reader {
            return InputStreamReader(newInputStream(ch), Charset.forName(csName))
        }

        actual fun newWriter(ch: WritableByteChannel, enc: CharsetEncoder, minBufferCap: Int): Writer {
            return OutputStreamWriter(newOutputStream(ch), enc.charset())
        }

        actual fun newWriter(ch: WritableByteChannel, csName: String): Writer {
            return OutputStreamWriter(newOutputStream(ch), Charset.forName(csName))
        }
    }
}
