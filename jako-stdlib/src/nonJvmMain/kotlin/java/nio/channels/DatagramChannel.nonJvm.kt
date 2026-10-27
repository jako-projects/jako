package java.nio.channels

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.SocketAddress
import java.net.SocketOption
import java.net.SocketTimeoutException
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

internal expect fun getDatagramSocketNativeHandle(datagramSocket: DatagramSocket): Long

actual abstract class DatagramChannel protected actual constructor() :
    SelectableChannel(), ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {

    actual override fun validOps(): Int = SelectionKey.OP_READ or SelectionKey.OP_WRITE

    actual abstract override fun bind(local: SocketAddress?): DatagramChannel
    actual abstract override fun <T> setOption(name: SocketOption<T>, value: T?): DatagramChannel
    actual abstract fun socket(): DatagramSocket
    actual abstract fun isConnected(): Boolean
    actual abstract fun connect(remote: SocketAddress): DatagramChannel
    actual abstract fun disconnect(): DatagramChannel
    actual abstract fun getRemoteAddress(): SocketAddress?
    actual abstract override fun getLocalAddress(): SocketAddress?
    actual abstract fun receive(dst: ByteBuffer): SocketAddress?
    actual abstract fun send(src: ByteBuffer, target: SocketAddress): Int
    actual abstract override fun read(dst: ByteBuffer): Int
    actual abstract override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    actual override fun read(dsts: Array<ByteBuffer>): Long = read(dsts, 0, dsts.size)
    actual abstract override fun write(src: ByteBuffer): Int
    actual abstract override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    actual override fun write(srcs: Array<ByteBuffer>): Long = write(srcs, 0, srcs.size)

    actual companion object {
        actual fun open(): DatagramChannel = DatagramChannelImpl(DatagramSocket(null as SocketAddress?))
    }
}

internal class DatagramChannelImpl(
    private val underlyingSocket: DatagramSocket
) : DatagramChannel() {
    private val lock = ReentrantLock()
    private var openState = !underlyingSocket.isClosed()
    private val optionValues = HashMap<SocketOption<*>, Any?>()

    override val isOpen: Boolean get() = isOpen()
    override fun isOpen(): Boolean = lock.withLock { openState && !underlyingSocket.isClosed() }

    override fun nativeSocketHandle(): Long =
        if (isOpen()) getDatagramSocketNativeHandle(underlyingSocket) else -1L

    override fun pollReadyOps(): Int {
        if (!isOpen()) return 0
        val handle = getDatagramSocketNativeHandle(underlyingSocket)
        if (handle == -1L) return 0
        var interest = SelectionKey.OP_WRITE
        if (underlyingSocket.isBound()) {
            interest = interest or SelectionKey.OP_READ
        }
        return pollNativeSocketHandle(handle, interest)
    }

    override fun bind(local: SocketAddress?): DatagramChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (underlyingSocket.isBound()) throw AlreadyBoundException()
        if (local != null && local !is InetSocketAddress) throw UnsupportedAddressTypeException()
        if (local is InetSocketAddress && local.isUnresolved()) throw UnresolvedAddressException()
        underlyingSocket.bind(local ?: InetSocketAddress(0))
        this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> setOption(name: SocketOption<T>, value: T?): DatagramChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        optionValues[name] = value
        when (name) {
            StandardSocketOptions.SO_SNDBUF -> underlyingSocket.setSendBufferSize(value as Int)
            StandardSocketOptions.SO_RCVBUF -> underlyingSocket.setReceiveBufferSize(value as Int)
            StandardSocketOptions.SO_REUSEADDR -> underlyingSocket.setReuseAddress(value as Boolean)
            StandardSocketOptions.SO_BROADCAST -> underlyingSocket.setBroadcast(value as Boolean)
        }
        this
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> getOption(name: SocketOption<T>): T? = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (optionValues.containsKey(name)) return@withLock optionValues[name] as T?
        when (name) {
            StandardSocketOptions.SO_SNDBUF -> underlyingSocket.getSendBufferSize() as T
            StandardSocketOptions.SO_RCVBUF -> underlyingSocket.getReceiveBufferSize() as T
            StandardSocketOptions.SO_REUSEADDR -> underlyingSocket.getReuseAddress() as T
            StandardSocketOptions.SO_BROADCAST -> underlyingSocket.getBroadcast() as T
            else -> throw UnsupportedOperationException("Unsupported option: ${name.name()}")
        }
    }

    override fun supportedOptions(): Set<SocketOption<*>> = setOf(
        StandardSocketOptions.SO_SNDBUF,
        StandardSocketOptions.SO_RCVBUF,
        StandardSocketOptions.SO_REUSEADDR,
        StandardSocketOptions.SO_BROADCAST
    )

    override fun socket(): DatagramSocket = underlyingSocket

    override fun isConnected(): Boolean = lock.withLock { isOpen() && underlyingSocket.isConnected() }

    override fun connect(remote: SocketAddress): DatagramChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (underlyingSocket.isConnected()) throw AlreadyConnectedException()
        if (remote !is InetSocketAddress) throw UnsupportedAddressTypeException()
        if (remote.isUnresolved()) throw UnresolvedAddressException()
        underlyingSocket.connect(remote)
        this
    }

    override fun disconnect(): DatagramChannel = lock.withLock {
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) return@withLock this
        underlyingSocket.disconnect()
        this
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

    override fun receive(dst: ByteBuffer): SocketAddress? {
        if (dst.isReadOnly()) throw ReadOnlyBufferException()
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isBound()) return null
        if (!isBlocking()) {
            if ((pollReadyOps() and SelectionKey.OP_READ) == 0) {
                return null
            }
        }
        val rem = dst.remaining()
        val pos = dst.position()
        val packet = DatagramPacket(dst.arrayData, dst.offset + pos, rem)
        try {
            underlyingSocket.receive(packet)
        } catch (_: SocketTimeoutException) {
            if (!isBlocking()) return null
            throw SocketTimeoutException()
        }
        val n = minOf(rem, packet.getLength())
        if (n > 0) {
            dst.position(pos + n)
        }
        return packet.getSocketAddress()
    }

    override fun send(src: ByteBuffer, target: SocketAddress): Int {
        if (!isOpen()) throw ClosedChannelException()
        if (target !is InetSocketAddress) throw UnsupportedAddressTypeException()
        if (target.isUnresolved()) throw UnresolvedAddressException()
        if (underlyingSocket.isConnected() && target != underlyingSocket.getRemoteSocketAddress()) {
            throw IllegalArgumentException("Connected address and target address differ")
        }
        val rem = src.remaining()
        val pos = src.position()
        val packet = DatagramPacket(src.arrayData, src.offset + pos, rem, target)
        underlyingSocket.send(packet)
        src.position(pos + rem)
        notifySelectors()
        return rem
    }

    override fun read(dst: ByteBuffer): Int {
        if (dst.isReadOnly()) throw ReadOnlyBufferException()
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        if (!isBlocking()) {
            if ((pollReadyOps() and SelectionKey.OP_READ) == 0) {
                return 0
            }
        }
        val rem = dst.remaining()
        val pos = dst.position()
        val packet = DatagramPacket(dst.arrayData, dst.offset + pos, rem)
        try {
            underlyingSocket.receive(packet)
        } catch (_: SocketTimeoutException) {
            if (!isBlocking()) return 0
            throw SocketTimeoutException()
        }
        val n = minOf(rem, packet.getLength())
        if (n > 0) {
            dst.position(pos + n)
        }
        return n
    }

    override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long {
        if (offset < 0 || length < 0 || offset > dsts.size - length) throw IndexOutOfBoundsException()
        for (i in offset until offset + length) {
            if (dsts[i].isReadOnly()) throw ReadOnlyBufferException()
        }
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        var totalCap = 0
        for (i in offset until offset + length) {
            totalCap += dsts[i].remaining()
        }
        val tmp = ByteBuffer.allocate(totalCap)
        val n = read(tmp)
        if (n <= 0) return n.toLong()
        tmp.flip()
        var copied = 0L
        for (i in offset until offset + length) {
            val dst = dsts[i]
            val chunk = minOf(dst.remaining(), tmp.remaining())
            if (chunk > 0) {
                val dstPos = dst.position()
                val tmpPos = tmp.position()
                tmp.arrayData.copyInto(dst.arrayData, dst.offset + dstPos, tmp.offset + tmpPos, tmp.offset + tmpPos + chunk)
                dst.position(dstPos + chunk)
                tmp.position(tmpPos + chunk)
                copied += chunk
            }
        }
        return copied
    }

    override fun write(src: ByteBuffer): Int {
        if (!isOpen()) throw ClosedChannelException()
        val remote = underlyingSocket.getRemoteSocketAddress() ?: throw NotYetConnectedException()
        return send(src, remote)
    }

    override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long {
        if (offset < 0 || length < 0 || offset > srcs.size - length) throw IndexOutOfBoundsException()
        if (!isOpen()) throw ClosedChannelException()
        val remote = underlyingSocket.getRemoteSocketAddress() ?: throw NotYetConnectedException()
        var totalRem = 0
        for (i in offset until offset + length) {
            totalRem += srcs[i].remaining()
        }
        val merged = ByteBuffer.allocate(totalRem)
        for (i in offset until offset + length) {
            merged.put(srcs[i])
        }
        merged.flip()
        return send(merged, remote).toLong()
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
