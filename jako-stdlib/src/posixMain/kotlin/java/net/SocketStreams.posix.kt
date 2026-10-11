@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import java.io.InputStream
import java.io.OutputStream
import kotlinx.cinterop.*
import platform.posix.*

internal class SocketInputStream(
    private val socket: Socket,
    private val fdGetter: () -> Int
) : InputStream() {

    override fun read(): Int {
        val b = ByteArray(1)
        val n = read(b, 0, 1)
        if (n <= 0) return -1
        return b[0].toInt() and 0xFF
    }

    override fun read(b: ByteArray, off: Int, len: Int): Int {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return 0
        val fd = fdGetter()
        if (fd < 0 || socket.isClosed()) throw SocketException("Socket is closed")

        return memScoped {
            val buf = allocArray<ByteVar>(len)
            val bytesRead = recv(fd, buf, len.convert(), 0).toInt()
            if (bytesRead < 0) {
                val err = errno
                if (err == EAGAIN || err == EWOULDBLOCK) {
                    throw SocketTimeoutException("Read timed out")
                }
                throw SocketException("recv failed: errno $err")
            }
            if (bytesRead == 0) {
                return -1 // EOF
            }
            for (i in 0 until bytesRead) {
                b[off + i] = buf[i]
            }
            bytesRead
        }
    }

    override fun close() {
        socket.shutdownInput()
    }
}

internal class SocketOutputStream(
    private val socket: Socket,
    private val fdGetter: () -> Int
) : OutputStream() {

    override fun write(b: Int) {
        val buf = byteArrayOf(b.toByte())
        write(buf, 0, 1)
    }

    override fun write(b: ByteArray, off: Int, len: Int) {
        if (off < 0 || len < 0 || len > b.size - off) {
            throw IndexOutOfBoundsException("off=$off, len=$len, b.size=${b.size}")
        }
        if (len == 0) return
        val fd = fdGetter()
        if (fd < 0 || socket.isClosed()) throw SocketException("Socket is closed")

        var totalSent = 0
        memScoped {
            val buf = allocArray<ByteVar>(len)
            for (i in 0 until len) {
                buf[i] = b[off + i]
            }
            while (totalSent < len) {
                val sent = send(fd, buf + totalSent, (len - totalSent).convert(), 0).toInt()
                if (sent < 0) {
                    val err = errno
                    if (err == EAGAIN || err == EWOULDBLOCK) {
                        throw SocketTimeoutException("Write timed out")
                    }
                    throw SocketException("send failed: errno $err")
                }
                totalSent += sent
            }
        }
    }

    override fun close() {
        socket.shutdownOutput()
    }
}
