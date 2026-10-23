package java.nio.channels

import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketOption
import java.net.SocketTimeoutException
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal expect fun getSocketNativeHandle(socket: Socket): Long

internal expect fun pollNativeSocketHandle(handle: Long, interestOps: Int): Int

actual abstract class SocketChannel protected actual constructor() :
    SelectableChannel(), ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {

    actual override fun validOps(): Int =
        SelectionKey.OP_READ or SelectionKey.OP_WRITE or SelectionKey.OP_CONNECT

    actual abstract override fun bind(local: SocketAddress?): SocketChannel
    actual abstract override fun <T> setOption(name: SocketOption<T>, value: T?): SocketChannel
    actual abstract fun shutdownInput(): SocketChannel
    actual abstract fun shutdownOutput(): SocketChannel
    actual abstract fun socket(): Socket
    actual abstract fun isConnected(): Boolean
    actual abstract fun isConnectionPending(): Boolean
    actual abstract fun connect(remote: SocketAddress): Boolean
    actual abstract fun finishConnect(): Boolean
    actual abstract fun getRemoteAddress(): SocketAddress?
    actual abstract override fun getLocalAddress(): SocketAddress?
    actual abstract override fun read(dst: ByteBuffer): Int
    actual abstract override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    actual override fun read(dsts: Array<ByteBuffer>): Long = read(dsts, 0, dsts.size)
    actual abstract override fun write(src: ByteBuffer): Int
    actual abstract override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    actual override fun write(srcs: Array<ByteBuffer>): Long = write(srcs, 0, srcs.size)

    actual companion object {
        actual fun open(): SocketChannel = SocketChannelImpl(Socket())

        actual fun open(remote: SocketAddress): SocketChannel {
            val ch = open()
            try {
                ch.connect(remote)
            } catch (t: Throwable) {
                try {
                    ch.close()
                } catch (_: Throwable) {
                }
                throw t
            }
            return ch
        }
    }
}

internal class SocketChannelImpl(
    private val underlyingSocket: Socket
) : SocketChannel() {
    private val lock = ReentrantLock()
    private var openState = !underlyingSocket.isClosed()
    private val optionValues = HashMap<SocketOption<*>, Any?>()

    override val isOpen: Boolean get() = isOpen()
    override fun isOpen(): Boolean = lock.withLock { openState && !underlyingSocket.isClosed() }

    override fun nativeSocketHandle(): Long =
        if (isOpen() && underlyingSocket.isConnected()) getSocketNativeHandle(underlyingSocket) else -1L

    override fun pollReadyOps(): Int {
        if (!isOpen() || !underlyingSocket.isConnected()) return 0
        val handle = getSocketNativeHandle(underlyingSocket)
        if (handle == -1L) return 0
        var interest = 0
        if (!underlyingSocket.isInputShutdown()) interest = interest or SelectionKey.OP_READ
        if (!underlyingSocket.isOutputShutdown()) interest = interest or SelectionKey.OP_WRITE
        if (interest == 0) return 0
        return pollNativeSocketHandle(handle, interest)
    }

    override fun bind(local: SocketAddress?): SocketChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (underlyingSocket.isBound()) throw AlreadyBoundException()
        if (local != null && local !is InetSocketAddress) throw UnsupportedAddressTypeException()
        if (local is InetSocketAddress && local.isUnresolved()) throw UnresolvedAddressException()
        underlyingSocket.bind(local)
        this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> setOption(name: SocketOption<T>, value: T?): SocketChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        optionValues[name] = value
        when (name) {
            StandardSocketOptions.SO_KEEPALIVE -> underlyingSocket.setKeepAlive(value as Boolean)
            StandardSocketOptions.SO_RCVBUF -> underlyingSocket.setReceiveBufferSize(value as Int)
            StandardSocketOptions.SO_SNDBUF -> underlyingSocket.setSendBufferSize(value as Int)
            StandardSocketOptions.SO_REUSEADDR -> underlyingSocket.setReuseAddress(value as Boolean)
            StandardSocketOptions.TCP_NODELAY -> underlyingSocket.setTcpNoDelay(value as Boolean)
            StandardSocketOptions.SO_LINGER -> {
                val v = value as Int
                underlyingSocket.setSoLinger(v >= 0, if (v >= 0) v else 0)
            }
        }
        this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getOption(name: SocketOption<T>): T? = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (optionValues.containsKey(name)) return@withLock optionValues[name] as T?
        when (name) {
            StandardSocketOptions.SO_KEEPALIVE -> underlyingSocket.getKeepAlive() as T
            StandardSocketOptions.SO_RCVBUF -> underlyingSocket.getReceiveBufferSize() as T
            StandardSocketOptions.SO_SNDBUF -> underlyingSocket.getSendBufferSize() as T
            StandardSocketOptions.SO_REUSEADDR -> underlyingSocket.getReuseAddress() as T
            StandardSocketOptions.TCP_NODELAY -> underlyingSocket.getTcpNoDelay() as T
            StandardSocketOptions.SO_LINGER -> underlyingSocket.getSoLinger() as T
            else -> throw UnsupportedOperationException("Unsupported option: ${name.name()}")
        }
    }

    override fun supportedOptions(): Set<SocketOption<*>> = setOf(
        StandardSocketOptions.SO_SNDBUF,
        StandardSocketOptions.SO_RCVBUF,
        StandardSocketOptions.SO_KEEPALIVE,
        StandardSocketOptions.SO_REUSEADDR,
        StandardSocketOptions.SO_LINGER,
        StandardSocketOptions.TCP_NODELAY
    )

    override fun shutdownInput(): SocketChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        underlyingSocket.shutdownInput()
        this
    }

    override fun shutdownOutput(): SocketChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        underlyingSocket.shutdownOutput()
        this
    }

    override fun socket(): Socket = underlyingSocket

    override fun isConnected(): Boolean = lock.withLock { isOpen() && underlyingSocket.isConnected() }

    override fun isConnectionPending(): Boolean = false

    override fun connect(remote: SocketAddress): Boolean = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (underlyingSocket.isConnected()) throw AlreadyConnectedException()
        if (remote !is InetSocketAddress) throw UnsupportedAddressTypeException()
        if (remote.isUnresolved()) throw UnresolvedAddressException()
        underlyingSocket.connect(remote)
        notifySelectors()
        true
    }

    override fun finishConnect(): Boolean = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (underlyingSocket.isConnected()) return@withLock true
        throw NoConnectionPendingException()
    }

    override fun getRemoteAddress(): SocketAddress? = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        underlyingSocket.getRemoteSocketAddress()
    }

    override fun getLocalAddress(): SocketAddress? = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isBound()) return@withLock null
        underlyingSocket.getLocalSocketAddress()
    }

    override fun read(dst: ByteBuffer): Int {
        if (dst.isReadOnly()) throw ReadOnlyBufferException()
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        if (underlyingSocket.isInputShutdown()) return -1
        val rem = dst.remaining()
        if (rem == 0) return 0
        if (!isBlocking()) {
            if ((pollReadyOps() and SelectionKey.OP_READ) == 0) {
                return 0
            }
        }
        val pos = dst.position()
        val n = try {
            underlyingSocket.getInputStream().read(dst.arrayData, dst.offset + pos, rem)
        } catch (_: SocketTimeoutException) {
            if (!isBlocking()) return 0
            throw SocketTimeoutException()
        }
        if (n < 0) return -1
        dst.position(pos + n)
        return n
    }

    override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long {
        if (offset < 0 || length < 0 || offset > dsts.size - length) throw IndexOutOfBoundsException()
        for (i in offset until offset + length) {
            if (dsts[i].isReadOnly()) throw ReadOnlyBufferException()
        }
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        var total = 0L
        for (i in offset until offset + length) {
            val dst = dsts[i]
            if (!dst.hasRemaining()) continue
            val n = read(dst)
            if (n < 0) return if (total == 0L) -1L else total
            if (n == 0) break
            total += n
            if (dst.hasRemaining()) break
        }
        return total
    }

    override fun write(src: ByteBuffer): Int {
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        if (underlyingSocket.isOutputShutdown()) throw ClosedChannelException()
        val rem = src.remaining()
        if (rem == 0) return 0
        val pos = src.position()
        underlyingSocket.getOutputStream().write(src.arrayData, src.offset + pos, rem)
        src.position(pos + rem)
        notifySelectors()
        return rem
    }

    override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long {
        if (offset < 0 || length < 0 || offset > srcs.size - length) throw IndexOutOfBoundsException()
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        var total = 0L
        for (i in offset until offset + length) {
            val src = srcs[i]
            if (!src.hasRemaining()) continue
            total += write(src)
        }
        return total
    }

    override fun close() {
        lock.withLock {
            if (!openState) return
            openState = false
            try {
                underlyingSocket.close()
            } finally {
                closeSelectableChannel()
            }
        }
    }
}
