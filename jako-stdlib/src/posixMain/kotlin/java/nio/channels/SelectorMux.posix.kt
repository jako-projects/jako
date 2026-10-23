@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.nio.channels

import java.net.DatagramSocket
import java.net.ServerSocket
import java.net.Socket
import kotlinx.cinterop.*
import platform.posix.*

internal actual fun getSocketNativeHandle(socket: Socket): Long =
    if (socket.isClosed() || socket.fd < 0) -1L else socket.fd.toLong()

internal actual fun getServerSocketNativeHandle(serverSocket: ServerSocket): Long =
    if (serverSocket.isClosed() || serverSocket.fd < 0) -1L else serverSocket.fd.toLong()

internal actual fun getDatagramSocketNativeHandle(datagramSocket: DatagramSocket): Long =
    if (datagramSocket.isClosed() || datagramSocket.fd < 0) -1L else datagramSocket.fd.toLong()

internal actual fun pollNativeSocketHandle(handle: Long, interestOps: Int): Int {
    val fd = handle.toInt()
    if (fd < 0 || interestOps == 0) return 0
    return memScoped {
        val pfd = alloc<pollfd>()
        pfd.fd = fd
        var ev = 0
        if ((interestOps and (SelectionKey.OP_READ or SelectionKey.OP_ACCEPT)) != 0) {
            ev = ev or POLLIN
        }
        if ((interestOps and (SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT)) != 0) {
            ev = ev or POLLOUT
        }
        pfd.events = ev.toShort()
        pfd.revents = 0
        val ret = poll(pfd.ptr, 1u, 0)
        if (ret <= 0) return@memScoped 0
        val rev = pfd.revents.toInt()
        if ((rev and POLLNVAL) != 0) return@memScoped 0
        var ready = 0
        if ((rev and (POLLIN or POLLHUP or POLLERR)) != 0) {
            ready = ready or (interestOps and (SelectionKey.OP_READ or SelectionKey.OP_ACCEPT))
        }
        if ((rev and (POLLOUT or POLLERR)) != 0) {
            ready = ready or (interestOps and (SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT))
        }
        ready
    }
}

internal actual class NativeSelectorMux actual constructor() {
    private var wakeupReadFd: Int = -1
    private var wakeupWriteFd: Int = -1

    init {
        memScoped {
            val fds = allocArray<IntVar>(2)
            if (pipe(fds) == 0) {
                wakeupReadFd = fds[0]
                wakeupWriteFd = fds[1]
                setFdNonBlocking(wakeupReadFd)
                setFdNonBlocking(wakeupWriteFd)
            }
        }
    }

    private fun setFdNonBlocking(fd: Int) {
        if (fd < 0) return
        val flags = fcntl(fd, F_GETFL, 0)
        if (flags >= 0) {
            fcntl(fd, F_SETFL, flags or O_NONBLOCK)
        }
    }

    actual fun await(handles: LongArray, interestOps: IntArray, count: Int, timeoutMs: Int): Boolean {
        val hasWakeup = wakeupReadFd >= 0
        val total = count + (if (hasWakeup) 1 else 0)
        if (total == 0) {
            if (timeoutMs > 0) {
                usleep((timeoutMs.coerceAtMost(50) * 1000).toUInt())
            }
            return false
        }
        return memScoped {
            val pfds = allocArray<pollfd>(total)
            for (i in 0 until count) {
                pfds[i].fd = handles[i].toInt()
                var ev = 0
                val ops = interestOps[i]
                if ((ops and (SelectionKey.OP_READ or SelectionKey.OP_ACCEPT)) != 0) {
                    ev = ev or POLLIN
                }
                if ((ops and (SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT)) != 0) {
                    ev = ev or POLLOUT
                }
                pfds[i].events = ev.toShort()
                pfds[i].revents = 0
            }
            if (hasWakeup) {
                pfds[count].fd = wakeupReadFd
                pfds[count].events = POLLIN.toShort()
                pfds[count].revents = 0
            }
            val ret = poll(pfds, total.convert(), timeoutMs)
            if (hasWakeup && (pfds[count].revents.toInt() and POLLIN) != 0) {
                clearWakeup()
            }
            ret > 0
        }
    }

    actual fun wakeup() {
        val wfd = wakeupWriteFd
        if (wfd >= 0) {
            memScoped {
                val b = alloc<ByteVar>()
                b.value = 1
                write(wfd, b.ptr, 1u)
            }
        }
    }

    actual fun clearWakeup() {
        val rfd = wakeupReadFd
        if (rfd >= 0) {
            memScoped {
                val buf = allocArray<ByteVar>(64)
                while (read(rfd, buf, 64u) > 0) {
                    // Drain wakeup pipe
                }
            }
        }
    }

    actual fun close() {
        val rfd = wakeupReadFd
        val wfd = wakeupWriteFd
        wakeupReadFd = -1
        wakeupWriteFd = -1
        if (rfd >= 0) platform.posix.close(rfd)
        if (wfd >= 0) platform.posix.close(wfd)
    }
}
