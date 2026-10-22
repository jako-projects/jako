@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package java.nio.channels

import java.net.DatagramSocket
import java.net.ServerSocket
import java.net.Socket
import java.net.WinsockInitializer
import java.net.mingwHtons
import kotlinx.cinterop.*
import platform.posix.*

internal actual fun getSocketNativeHandle(socket: Socket): Long {
    val s = socket.socketHandle
    return if (socket.isClosed() || s == INVALID_SOCKET.convert<ULong>()) -1L else s.toLong()
}

internal actual fun getServerSocketNativeHandle(serverSocket: ServerSocket): Long {
    val s = serverSocket.sock
    return if (serverSocket.isClosed() || s == INVALID_SOCKET.convert<ULong>()) -1L else s.toLong()
}

internal actual fun getDatagramSocketNativeHandle(datagramSocket: DatagramSocket): Long {
    val s = datagramSocket.sock
    return if (datagramSocket.isClosed() || s == INVALID_SOCKET.convert<ULong>()) -1L else s.toLong()
}

internal actual fun pollNativeSocketHandle(handle: Long, interestOps: Int): Int {
    if (handle == -1L || interestOps == 0) return 0
    val sock: ULong = handle.toULong()
    if (sock == INVALID_SOCKET.convert<ULong>()) return 0
    return memScoped {
        val readSet = alloc<fd_set>()
        val writeSet = alloc<fd_set>()
        val exceptSet = alloc<fd_set>()
        readSet.fd_count = 0u
        writeSet.fd_count = 0u
        exceptSet.fd_count = 1u
        exceptSet.fd_array[0] = sock.convert()

        if ((interestOps and (SelectionKey.OP_READ or SelectionKey.OP_ACCEPT)) != 0) {
            readSet.fd_count = 1u
            readSet.fd_array[0] = sock.convert()
        }
        if ((interestOps and (SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT)) != 0) {
            writeSet.fd_count = 1u
            writeSet.fd_array[0] = sock.convert()
        }

        val tv = alloc<timeval>()
        tv.tv_sec = 0
        tv.tv_usec = 0
        val sel = platform.windows.select(
            0,
            readSet.ptr.reinterpret(),
            writeSet.ptr.reinterpret(),
            exceptSet.ptr.reinterpret(),
            tv.ptr.reinterpret()
        )
        if (sel <= 0) return@memScoped 0
        var ready = 0
        if (readSet.fd_count > 0u || exceptSet.fd_count > 0u) {
            ready = ready or (interestOps and (SelectionKey.OP_READ or SelectionKey.OP_ACCEPT))
        }
        if (writeSet.fd_count > 0u || exceptSet.fd_count > 0u) {
            ready = ready or (interestOps and (SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT))
        }
        ready
    }
}

internal actual class NativeSelectorMux actual constructor() {
    private var wakeupSock: ULong = INVALID_SOCKET.convert()

    init {
        WinsockInitializer.ensureInitialized()
        memScoped {
            val s = socket(AF_INET, SOCK_DGRAM, 0)
            if (s != INVALID_SOCKET.convert<ULong>()) {
                val sin = alloc<sockaddr_in>()
                sin.sin_family = AF_INET.convert()
                sin.sin_port = mingwHtons(0u)
                sin.sin_addr.S_un.S_addr = 0x0100007Fu // 127.0.0.1 in network byte order (little-endian host)
                if (bind(s, sin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) == 0) {
                    val localSin = alloc<sockaddr_in>()
                    val len = alloc<IntVar>()
                    len.value = sizeOf<sockaddr_in>().convert()
                    if (getsockname(s, localSin.ptr.reinterpret(), len.ptr) == 0 &&
                        connect(s, localSin.ptr.reinterpret(), sizeOf<sockaddr_in>().convert()) == 0
                    ) {
                        wakeupSock = s.convert()
                    } else {
                        closesocket(s)
                    }
                } else {
                    closesocket(s)
                }
            }
        }
    }

    actual fun await(handles: LongArray, interestOps: IntArray, count: Int, timeoutMs: Int): Boolean {
        val ws = wakeupSock
        val hasWakeup = ws != INVALID_SOCKET.convert<ULong>()
        if (count == 0 && !hasWakeup) {
            if (timeoutMs > 0) {
                platform.windows.Sleep(timeoutMs.coerceAtMost(50).toUInt())
            }
            return false
        }
        return memScoped {
            val readSet = alloc<fd_set>()
            val writeSet = alloc<fd_set>()
            val exceptSet = alloc<fd_set>()
            readSet.fd_count = 0u
            writeSet.fd_count = 0u
            exceptSet.fd_count = 0u

            if (hasWakeup) {
                readSet.fd_array[0] = ws.convert()
                readSet.fd_count = 1u
            }

            val maxSockets = 60
            val limit = minOf(count, maxSockets)
            for (i in 0 until limit) {
                val s: ULong = handles[i].toULong()
                if (s == INVALID_SOCKET.convert<ULong>()) continue
                val ops = interestOps[i]
                if ((ops and (SelectionKey.OP_READ or SelectionKey.OP_ACCEPT)) != 0) {
                    val idx = readSet.fd_count.toInt()
                    if (idx < 64) {
                        readSet.fd_array[idx] = s.convert()
                        readSet.fd_count = (idx + 1).toUInt()
                    }
                }
                if ((ops and (SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT)) != 0) {
                    val idx = writeSet.fd_count.toInt()
                    if (idx < 64) {
                        writeSet.fd_array[idx] = s.convert()
                        writeSet.fd_count = (idx + 1).toUInt()
                    }
                }
                val eIdx = exceptSet.fd_count.toInt()
                if (eIdx < 64) {
                    exceptSet.fd_array[eIdx] = s.convert()
                    exceptSet.fd_count = (eIdx + 1).toUInt()
                }
            }

            val sel = if (timeoutMs >= 0) {
                val tv = alloc<timeval>()
                tv.tv_sec = timeoutMs / 1000
                tv.tv_usec = (timeoutMs % 1000) * 1000
                platform.windows.select(
                    0,
                    readSet.ptr.reinterpret(),
                    writeSet.ptr.reinterpret(),
                    exceptSet.ptr.reinterpret(),
                    tv.ptr.reinterpret()
                )
            } else {
                platform.windows.select(
                    0,
                    readSet.ptr.reinterpret(),
                    writeSet.ptr.reinterpret(),
                    exceptSet.ptr.reinterpret(),
                    null
                )
            }
            if (sel > 0 && hasWakeup) {
                for (i in 0 until readSet.fd_count.toInt()) {
                    if (readSet.fd_array[i].toULong() == ws) {
                        clearWakeup()
                        break
                    }
                }
            }
            sel > 0
        }
    }

    actual fun wakeup() {
        val ws = wakeupSock
        if (ws != INVALID_SOCKET.convert<ULong>()) {
            memScoped {
                val b = alloc<ByteVar>()
                b.value = 1
                send(ws.convert(), b.ptr, 1, 0)
            }
        }
    }

    actual fun clearWakeup() {
        val ws = wakeupSock
        if (ws != INVALID_SOCKET.convert<ULong>()) {
            memScoped {
                val buf = allocArray<ByteVar>(64)
                while (true) {
                    val readSet = alloc<fd_set>()
                    readSet.fd_count = 1u
                    readSet.fd_array[0] = ws.convert()
                    val tv = alloc<timeval>()
                    tv.tv_sec = 0
                    tv.tv_usec = 0
                    val sel = platform.windows.select(
                        0,
                        readSet.ptr.reinterpret(),
                        null,
                        null,
                        tv.ptr.reinterpret()
                    )
                    if (sel <= 0) break
                    if (recv(ws.convert(), buf, 64, 0) <= 0) break
                }
            }
        }
    }

    actual fun close() {
        val ws = wakeupSock
        wakeupSock = INVALID_SOCKET.convert()
        if (ws != INVALID_SOCKET.convert<ULong>()) {
            closesocket(ws.convert())
        }
    }
}
