package java.nio.channels

import java.io.Closeable
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketAddress
import java.net.SocketOption
import java.net.SocketTimeoutException
import java.net.StandardSocketOptions
import java.nio.ByteBuffer
import java.nio.ReadOnlyBufferException
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

actual open class AlreadyBoundException actual constructor() : IllegalStateException()

actual open class AlreadyConnectedException actual constructor() : IllegalStateException()

actual open class ConnectionPendingException actual constructor() : IllegalStateException()

actual open class NoConnectionPendingException actual constructor() : IllegalStateException()

actual open class NotYetBoundException actual constructor() : IllegalStateException()

actual open class NotYetConnectedException actual constructor() : IllegalStateException()

actual open class UnresolvedAddressException actual constructor() : IllegalArgumentException()

actual open class UnsupportedAddressTypeException actual constructor() : IllegalArgumentException()

actual open class CancelledKeyException actual constructor() : IllegalStateException()

actual open class ClosedSelectorException actual constructor() : IllegalStateException()

actual open class IllegalBlockingModeException actual constructor() : IllegalStateException()

actual open class IllegalSelectorException actual constructor() : IllegalArgumentException()

actual interface NetworkChannel : Channel {
    actual fun bind(local: SocketAddress?): NetworkChannel
    actual fun getLocalAddress(): SocketAddress?
    actual fun <T> setOption(name: SocketOption<T>, value: T?): NetworkChannel
    actual fun <T> getOption(name: SocketOption<T>): T?
    actual fun supportedOptions(): Set<SocketOption<*>>
}

actual abstract class SelectableChannel protected actual constructor() : Channel, InterruptibleChannel {
    private val regLock = ReentrantLock()
    private val blockLockObj = Any()
    private var blockingMode = true
    private val registrations = mutableListOf<SelectionKeyImpl>()

    actual abstract fun validOps(): Int

    internal open fun pollReadyOps(): Int = 0

    internal fun notifySelectors() {
        val keysSnapshot = regLock.withLock { registrations.toList() }
        for (key in keysSnapshot) {
            if (key.isValid()) {
                key.selectorImpl.wakeup()
            }
        }
    }

    internal fun closeSelectableChannel() {
        val keysSnapshot = regLock.withLock {
            val copy = registrations.toList()
            registrations.clear()
            copy
        }
        for (key in keysSnapshot) {
            key.cancel()
            key.selectorImpl.wakeup()
        }
    }

    internal fun removeKey(key: SelectionKeyImpl) {
        regLock.withLock {
            registrations.remove(key)
        }
    }

    actual open fun isRegistered(): Boolean = regLock.withLock {
        registrations.any { it.isValid() }
    }

    actual open fun keyFor(sel: Selector): SelectionKey? = regLock.withLock {
        registrations.firstOrNull { it.selector() === sel && it.isValid() }
    }

    actual open fun register(sel: Selector, ops: Int, att: Any?): SelectionKey {
        if (!isOpen()) throw ClosedChannelException()
        if (!sel.isOpen()) throw ClosedSelectorException()
        if (sel !is SelectorImpl) throw IllegalSelectorException()
        if ((ops and validOps().inv()) != 0) {
            throw IllegalArgumentException("Invalid interestOps: $ops (validOps=${validOps()})")
        }
        return regLock.withLock {
            if (blockingMode) throw IllegalBlockingModeException()
            if (!isOpen()) throw ClosedChannelException()
            val existing = registrations.firstOrNull { it.selector() === sel }
            if (existing != null) {
                if (!existing.isValid()) throw CancelledKeyException()
                existing.interestOps(ops)
                existing.attach(att)
                return@withLock existing
            }
            val key = SelectionKeyImpl(this, sel, ops, att)
            registrations.add(key)
            sel.registerKey(key)
            key
        }
    }

    actual fun register(sel: Selector, ops: Int): SelectionKey = register(sel, ops, null)

    actual open fun configureBlocking(block: Boolean): SelectableChannel {
        if (!isOpen()) throw ClosedChannelException()
        regLock.withLock {
            if (block && registrations.any { it.isValid() }) {
                throw IllegalBlockingModeException()
            }
            blockingMode = block
        }
        return this
    }

    actual open fun isBlocking(): Boolean = regLock.withLock { blockingMode }

    actual open fun blockingLock(): Any = blockLockObj
}

actual abstract class SelectionKey protected actual constructor() {
    private val attLock = ReentrantLock()
    private var attachedObj: Any? = null

    actual abstract fun channel(): SelectableChannel
    actual abstract fun selector(): Selector
    actual abstract fun isValid(): Boolean
    actual abstract fun cancel()
    actual abstract fun interestOps(): Int
    actual abstract fun interestOps(ops: Int): SelectionKey
    actual abstract fun readyOps(): Int

    actual fun isReadable(): Boolean {
        if (!isValid()) throw CancelledKeyException()
        return (readyOps() and OP_READ) != 0
    }

    actual fun isWritable(): Boolean {
        if (!isValid()) throw CancelledKeyException()
        return (readyOps() and OP_WRITE) != 0
    }

    actual fun isConnectable(): Boolean {
        if (!isValid()) throw CancelledKeyException()
        return (readyOps() and OP_CONNECT) != 0
    }

    actual fun isAcceptable(): Boolean {
        if (!isValid()) throw CancelledKeyException()
        return (readyOps() and OP_ACCEPT) != 0
    }

    actual fun attach(ob: Any?): Any? = attLock.withLock {
        val prev = attachedObj
        attachedObj = ob
        prev
    }

    actual fun attachment(): Any? = attLock.withLock { attachedObj }

    actual companion object {
        actual val OP_READ: Int = 1 shl 0
        actual val OP_WRITE: Int = 1 shl 2
        actual val OP_CONNECT: Int = 1 shl 3
        actual val OP_ACCEPT: Int = 1 shl 4
    }
}

internal class SelectionKeyImpl(
    private val ch: SelectableChannel,
    internal val selectorImpl: SelectorImpl,
    initialInterestOps: Int,
    initialAttachment: Any?
) : SelectionKey() {
    private val lock = ReentrantLock()
    private var validFlag = true
    private var interest = initialInterestOps
    internal var ready = 0

    init {
        attach(initialAttachment)
    }

    override fun channel(): SelectableChannel = ch
    override fun selector(): Selector = selectorImpl

    override fun isValid(): Boolean = lock.withLock { validFlag && selectorImpl.isOpen() && ch.isOpen() }

    override fun cancel() {
        val shouldNotify = lock.withLock {
            if (!validFlag) return@withLock false
            validFlag = false
            true
        }
        if (shouldNotify) {
            selectorImpl.enqueueCancelledKey(this)
        }
    }

    override fun interestOps(): Int = lock.withLock {
        if (!isValid()) throw CancelledKeyException()
        interest
    }

    override fun interestOps(ops: Int): SelectionKey {
        if ((ops and ch.validOps().inv()) != 0) {
            throw IllegalArgumentException("Invalid ops: $ops")
        }
        lock.withLock {
            if (!isValid()) throw CancelledKeyException()
            interest = ops
        }
        selectorImpl.wakeup()
        return this
    }

    override fun readyOps(): Int = lock.withLock {
        if (!isValid()) throw CancelledKeyException()
        ready
    }

    internal fun setReadyOpsInternal(ops: Int) = lock.withLock {
        ready = ops
    }

    internal fun getReadyOpsInternal(): Int = lock.withLock { ready }
    internal fun getInterestOpsInternal(): Int = lock.withLock { interest }
    internal fun invalidateDirectly() = lock.withLock { validFlag = false }
}

actual abstract class Selector protected actual constructor() : Closeable {
    actual abstract fun isOpen(): Boolean
    actual abstract fun keys(): MutableSet<SelectionKey>
    actual abstract fun selectedKeys(): MutableSet<SelectionKey>
    actual abstract fun selectNow(): Int
    actual abstract fun select(timeout: Long): Int
    actual abstract fun select(): Int
    actual abstract fun wakeup(): Selector
    actual abstract override fun close()

    actual companion object {
        actual fun open(): Selector = SelectorImpl()
    }
}

internal class SelectorImpl : Selector() {
    private val lock = ReentrantLock()
    private val cond = lock.newCondition()
    private var openFlag = true
    private var wakeupFlag = false
    private val keySet = LinkedHashSet<SelectionKey>()
    private val selectedKeySet = LinkedHashSet<SelectionKey>()
    private val cancelledKeys = LinkedHashSet<SelectionKeyImpl>()

    internal fun registerKey(key: SelectionKeyImpl) {
        lock.withLock {
            if (!openFlag) throw ClosedSelectorException()
            keySet.add(key)
            cond.signalAll()
        }
    }

    internal fun enqueueCancelledKey(key: SelectionKeyImpl) {
        lock.withLock {
            cancelledKeys.add(key)
            cond.signalAll()
        }
    }

    private fun processCancelledKeysLocked() {
        if (cancelledKeys.isEmpty()) return
        for (k in cancelledKeys) {
            keySet.remove(k)
            selectedKeySet.remove(k)
            k.channel().removeKey(k)
        }
        cancelledKeys.clear()
    }

    private fun doSelectLocked(): Int {
        processCancelledKeysLocked()
        var updatedCount = 0
        for (rawKey in keySet.toList()) {
            val key = rawKey as SelectionKeyImpl
            if (!key.isValid()) {
                cancelledKeys.add(key)
                continue
            }
            val interest = key.getInterestOpsInternal()
            if (interest == 0) continue
            val ready = key.channel().pollReadyOps() and interest
            if (ready != 0) {
                if (selectedKeySet.add(key)) {
                    key.setReadyOpsInternal(ready)
                    updatedCount++
                } else {
                    val prev = key.getReadyOpsInternal()
                    val merged = prev or ready
                    key.setReadyOpsInternal(merged)
                    if (merged != prev) {
                        updatedCount++
                    }
                }
            }
        }
        processCancelledKeysLocked()
        return updatedCount
    }

    override fun isOpen(): Boolean = lock.withLock { openFlag }

    override fun keys(): MutableSet<SelectionKey> = lock.withLock {
        if (!openFlag) throw ClosedSelectorException()
        keySet
    }

    override fun selectedKeys(): MutableSet<SelectionKey> = lock.withLock {
        if (!openFlag) throw ClosedSelectorException()
        selectedKeySet
    }

    override fun selectNow(): Int = lock.withLock {
        if (!openFlag) throw ClosedSelectorException()
        wakeupFlag = false
        doSelectLocked()
    }

    override fun select(timeout: Long): Int {
        if (timeout < 0L) throw IllegalArgumentException("Negative timeout: $timeout")
        return lock.withLock {
            if (!openFlag) throw ClosedSelectorException()
            val deadline = if (timeout > 0L) java.lang.System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeout) else 0L
            while (true) {
                if (!openFlag) throw ClosedSelectorException()
                val n = doSelectLocked()
                if (n > 0 || wakeupFlag) {
                    wakeupFlag = false
                    return@withLock n
                }
                if (timeout > 0L) {
                    val remaining = deadline - java.lang.System.nanoTime()
                    if (remaining <= 0L) {
                        return@withLock 0
                    }
                    cond.awaitNanos(minOf(remaining, TimeUnit.MILLISECONDS.toNanos(5)))
                } else {
                    cond.awaitNanos(TimeUnit.MILLISECONDS.toNanos(5))
                }
            }
            @Suppress("UNREACHABLE_CODE")
            0
        }
    }

    override fun select(): Int = select(0L)

    override fun wakeup(): Selector = lock.withLock {
        wakeupFlag = true
        cond.signalAll()
        this
    }

    override fun close() {
        val keysToClose = lock.withLock {
            if (!openFlag) return
            openFlag = false
            wakeupFlag = true
            val list = keySet.toList()
            keySet.clear()
            selectedKeySet.clear()
            cancelledKeys.clear()
            cond.signalAll()
            list
        }
        for (k in keysToClose) {
            if (k is SelectionKeyImpl) {
                k.invalidateDirectly()
                k.channel().removeKey(k)
            }
        }
    }
}

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

    override fun pollReadyOps(): Int {
        if (!isOpen()) return 0
        var ops = 0
        if (underlyingSocket.isConnected()) {
            if (!underlyingSocket.isOutputShutdown()) {
                ops = ops or SelectionKey.OP_WRITE
            }
            if (!underlyingSocket.isInputShutdown() && underlyingSocket.getInputStream().available() > 0) {
                ops = ops or SelectionKey.OP_READ
            }
        }
        return ops
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
        val tmp = ByteArray(minOf(rem, 8192))
        val prevTimeout = underlyingSocket.getSoTimeout()
        try {
            if (!isBlocking()) {
                underlyingSocket.setSoTimeout(1)
            }
            val n = try {
                underlyingSocket.getInputStream().read(tmp, 0, tmp.size)
            } catch (_: SocketTimeoutException) {
                if (!isBlocking()) return 0
                throw SocketTimeoutException()
            }
            if (n < 0) return -1
            dst.put(tmp, 0, n)
            return n
        } finally {
            if (!isBlocking() && !underlyingSocket.isClosed()) {
                underlyingSocket.setSoTimeout(prevTimeout)
            }
        }
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
        val tmp = ByteArray(rem)
        src.get(tmp)
        underlyingSocket.getOutputStream().write(tmp, 0, rem)
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

    override fun pollReadyOps(): Int {
        if (!isOpen() || !underlyingServerSocket.isBound()) return 0
        return if (java.net.serverSocketHasPendingConnection(underlyingServerSocket)) SelectionKey.OP_ACCEPT else 0
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
        val prevTimeout = underlyingServerSocket.getSoTimeout()
        try {
            if (!isBlocking()) {
                underlyingServerSocket.setSoTimeout(1)
            }
            val accepted = try {
                underlyingServerSocket.accept()
            } catch (_: SocketTimeoutException) {
                if (!isBlocking()) return null
                throw SocketTimeoutException()
            }
            return SocketChannelImpl(accepted)
        } finally {
            if (!isBlocking() && !underlyingServerSocket.isClosed()) {
                underlyingServerSocket.setSoTimeout(prevTimeout)
            }
        }
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

    override fun pollReadyOps(): Int {
        if (!isOpen()) return 0
        var ops = SelectionKey.OP_WRITE
        if (underlyingSocket.isBound()) {
            ops = ops or SelectionKey.OP_READ
        }
        return ops
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
        val rem = dst.remaining()
        val buf = ByteArray(rem)
        val packet = DatagramPacket(buf, buf.size)
        val prevTimeout = underlyingSocket.getSoTimeout()
        try {
            if (!isBlocking()) {
                underlyingSocket.setSoTimeout(1)
            }
            try {
                underlyingSocket.receive(packet)
            } catch (_: SocketTimeoutException) {
                if (!isBlocking()) return null
                throw SocketTimeoutException()
            }
            val n = minOf(rem, packet.getLength())
            if (n > 0) {
                dst.put(buf, 0, n)
            }
            return packet.getSocketAddress()
        } finally {
            if (!isBlocking() && !underlyingSocket.isClosed()) {
                underlyingSocket.setSoTimeout(prevTimeout)
            }
        }
    }

    override fun send(src: ByteBuffer, target: SocketAddress): Int {
        if (!isOpen()) throw ClosedChannelException()
        if (target !is InetSocketAddress) throw UnsupportedAddressTypeException()
        if (target.isUnresolved()) throw UnresolvedAddressException()
        if (underlyingSocket.isConnected() && target != underlyingSocket.getRemoteSocketAddress()) {
            throw IllegalArgumentException("Connected address and target address differ")
        }
        val rem = src.remaining()
        val buf = ByteArray(rem)
        src.get(buf)
        val packet = DatagramPacket(buf, 0, rem, target)
        underlyingSocket.send(packet)
        notifySelectors()
        return rem
    }

    override fun read(dst: ByteBuffer): Int {
        if (dst.isReadOnly()) throw ReadOnlyBufferException()
        if (!isOpen()) throw ClosedChannelException()
        if (!underlyingSocket.isConnected()) throw NotYetConnectedException()
        val rem = dst.remaining()
        val buf = ByteArray(rem)
        val packet = DatagramPacket(buf, buf.size)
        val prevTimeout = underlyingSocket.getSoTimeout()
        try {
            if (!isBlocking()) {
                underlyingSocket.setSoTimeout(1)
            }
            try {
                underlyingSocket.receive(packet)
            } catch (_: SocketTimeoutException) {
                if (!isBlocking()) return 0
                throw SocketTimeoutException()
            }
            val n = minOf(rem, packet.getLength())
            if (n > 0) {
                dst.put(buf, 0, n)
            }
            return n
        } finally {
            if (!isBlocking() && !underlyingSocket.isClosed()) {
                underlyingSocket.setSoTimeout(prevTimeout)
            }
        }
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
            while (dst.hasRemaining() && tmp.hasRemaining()) {
                dst.put(tmp.get())
                copied++
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
