package java.nio.channels

import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.SocketAddress
import java.net.SocketOption
import java.net.SocketTimeoutException
import java.net.StandardSocketOptions
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal expect fun getServerSocketNativeHandle(serverSocket: ServerSocket): Long

actual abstract class ServerSocketChannel protected actual constructor() :
    SelectableChannel(), NetworkChannel {

    actual override fun validOps(): Int = SelectionKey.OP_ACCEPT

    actual override fun bind(local: SocketAddress?): ServerSocketChannel = bind(local, 0)
    actual abstract fun bind(local: SocketAddress?, backlog: Int): ServerSocketChannel
    actual abstract override fun <T> setOption(name: SocketOption<T>, value: T?): ServerSocketChannel
    actual abstract fun socket(): ServerSocket
    actual abstract fun accept(): SocketChannel?
    actual abstract override fun getLocalAddress(): SocketAddress?

    actual companion object {
        actual fun open(): ServerSocketChannel = ServerSocketChannelImpl(ServerSocket())
    }
}

internal class ServerSocketChannelImpl(
    private val underlyingServerSocket: ServerSocket
) : ServerSocketChannel() {
    private val lock = ReentrantLock()
    private var openState = !underlyingServerSocket.isClosed()
    private val optionValues = HashMap<SocketOption<*>, Any?>()

    override val isOpen: Boolean get() = isOpen()
    override fun isOpen(): Boolean = lock.withLock { openState && !underlyingServerSocket.isClosed() }

    override fun nativeSocketHandle(): Long =
        if (isOpen() && underlyingServerSocket.isBound()) getServerSocketNativeHandle(underlyingServerSocket) else -1L

    override fun pollReadyOps(): Int {
        if (!isOpen() || !underlyingServerSocket.isBound()) return 0
        val handle = getServerSocketNativeHandle(underlyingServerSocket)
        if (handle == -1L) return 0
        return pollNativeSocketHandle(handle, SelectionKey.OP_ACCEPT)
    }

    override fun bind(local: SocketAddress?, backlog: Int): ServerSocketChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (underlyingServerSocket.isBound()) throw AlreadyBoundException()
        if (local != null && local !is InetSocketAddress) throw UnsupportedAddressTypeException()
        if (local is InetSocketAddress && local.isUnresolved()) throw UnresolvedAddressException()
        underlyingServerSocket.bind(local ?: InetSocketAddress(0), backlog)
        notifySelectors()
        this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> setOption(name: SocketOption<T>, value: T?): ServerSocketChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        optionValues[name] = value
        when (name) {
            StandardSocketOptions.SO_RCVBUF -> underlyingServerSocket.setReceiveBufferSize(value as Int)
            StandardSocketOptions.SO_REUSEADDR -> underlyingServerSocket.setReuseAddress(value as Boolean)
        }
        this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getOption(name: SocketOption<T>): T? = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (optionValues.containsKey(name)) return@withLock optionValues[name] as T?
        when (name) {
            StandardSocketOptions.SO_RCVBUF -> underlyingServerSocket.getReceiveBufferSize() as T
            StandardSocketOptions.SO_REUSEADDR -> underlyingServerSocket.getReuseAddress() as T
            else -> throw UnsupportedOperationException("Unsupported option: ${name.name()}")
        }
    }

    override fun supportedOptions(): Set<SocketOption<*>> = setOf(
        StandardSocketOptions.SO_RCVBUF,
        StandardSocketOptions.SO_REUSEADDR
    )

    override fun socket(): ServerSocket = underlyingServerSocket

    override fun accept(): SocketChannel? {
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingServerSocket.isBound()) throw NotYetBoundException()
        if (!isBlocking()) {
            if ((pollReadyOps() and SelectionKey.OP_ACCEPT) == 0) {
                return null
            }
        }
        val accepted = try {
            underlyingServerSocket.accept()
        } catch (_: SocketTimeoutException) {
            if (!isBlocking()) return null
            throw SocketTimeoutException()
        }
        return SocketChannelImpl(accepted)
    }

    override fun getLocalAddress(): SocketAddress? = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingServerSocket.isBound()) return@withLock null
        underlyingServerSocket.getLocalSocketAddress()
    }

    override fun close() {
        lock.withLock {
            if (!openState) return
            openState = false
            try {
                underlyingServerSocket.close()
            } finally {
                closeSelectableChannel()
            }
        }
    }
}
