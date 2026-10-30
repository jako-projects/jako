@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.net

import java.io.InputStream
import java.io.OutputStream
import kotlinx.cinterop.*
import platform.posix.*

private val sigPipeIgnored: Boolean = run {
    signal(SIGPIPE, SIG_IGN)
    true
}

internal fun ensureSigPipeIgnored() {
    if (!sigPipeIgnored) {
        signal(SIGPIPE, SIG_IGN)
    }
}

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
            val timeoutMs = socket.getSoTimeout()
            if (timeoutMs > 0) {
                val readSet = alloc<fd_set>()
                posix_fd_zero(readSet.ptr)
                posix_fd_set(fd, readSet.ptr)
                val tv = alloc<timeval>()
                tv.tv_sec = (timeoutMs / 1000).convert()
                tv.tv_usec = ((timeoutMs % 1000) * 1000).convert()
                val sel = select(fd + 1, readSet.ptr, null, null, tv.ptr)
                if (sel == 0) {
                    throw SocketTimeoutException("Read timed out")
                } else if (sel < 0) {
                    throw SocketException("select failed: errno $errno")
                }
            }
            val bytesRead = b.usePinned { pinned ->
                recv(fd, pinned.addressOf(off), len.convert(), 0).toInt()
            }
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
            bytesRead
        }
    }

    override fun available(): Int {
        val fd = fdGetter()
        if (fd < 0 || socket.isClosed()) return 0
        return memScoped {
            val readSet = alloc<fd_set>()
            posix_fd_zero(readSet.ptr)
            posix_fd_set(fd, readSet.ptr)
            val tv = alloc<timeval>()
            tv.tv_sec = 0.convert()
            tv.tv_usec = 0.convert()
            val sel = select(fd + 1, readSet.ptr, null, null, tv.ptr)
            if (sel > 0) 1 else 0
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
        ensureSigPipeIgnored()

        var totalSent = 0
        b.usePinned { pinned ->
            while (totalSent < len) {
                val sent = send(fd, pinned.addressOf(off + totalSent), (len - totalSent).convert(), 0).toInt()
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
