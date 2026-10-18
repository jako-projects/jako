@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.io.Closeable
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.nio.ByteBuffer
import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder
import java.nio.file.OpenOption
import java.nio.file.Path

expect interface Channel : Closeable {
    fun isOpen(): Boolean
    override fun close()

    val isOpen: Boolean
}

expect interface InterruptibleChannel : Channel {
    override fun close()
}

expect interface ReadableByteChannel : Channel {
    /** [Nullability Decision] Java ReadableByteChannel.read(ByteBuffer) dst is non-null. */
    fun read(dst: ByteBuffer): Int
}

expect interface WritableByteChannel : Channel {
    /** [Nullability Decision] Java WritableByteChannel.write(ByteBuffer) src is non-null. */
    fun write(src: ByteBuffer): Int
}

expect interface ByteChannel : ReadableByteChannel, WritableByteChannel

expect interface SeekableByteChannel : ByteChannel {
    override fun read(dst: ByteBuffer): Int
    override fun write(src: ByteBuffer): Int
    fun position(): Long
    fun position(newPosition: Long): SeekableByteChannel
    fun size(): Long
    fun truncate(size: Long): SeekableByteChannel
}

expect interface ScatteringByteChannel : ReadableByteChannel {
    fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    fun read(dsts: Array<ByteBuffer>): Long
}

expect interface GatheringByteChannel : WritableByteChannel {
    fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    fun write(srcs: Array<ByteBuffer>): Long
}

/**
 * [Nullability Decision] FileLock.channel() and acquiredBy() return non-null channel instances.
 */
expect abstract class FileLock : AutoCloseable {
    protected constructor(channel: FileChannel, position: Long, size: Long, shared: Boolean)

    fun channel(): FileChannel
    open fun acquiredBy(): Channel
    fun position(): Long
    fun size(): Long
    fun isShared(): Boolean
    val isShared: Boolean
    fun overlaps(position: Long, size: Long): Boolean
    abstract fun isValid(): Boolean
    val isValid: Boolean
    abstract fun release()
    override fun close()
}

expect open class OverlappingFileLockException : IllegalStateException {
    constructor()
}

expect abstract class FileChannel : SeekableByteChannel, GatheringByteChannel, ScatteringByteChannel, ByteChannel, InterruptibleChannel {
    protected constructor()

    abstract override fun read(dst: ByteBuffer): Int
    abstract override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun read(dsts: Array<ByteBuffer>): Long
    abstract fun read(dst: ByteBuffer, position: Long): Int
    abstract override fun write(src: ByteBuffer): Int
    abstract override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun write(srcs: Array<ByteBuffer>): Long
    abstract fun write(src: ByteBuffer, position: Long): Int
    abstract override fun position(): Long
    abstract override fun position(newPosition: Long): FileChannel
    abstract override fun size(): Long
    abstract override fun truncate(size: Long): FileChannel
    abstract fun force(metaData: Boolean)
    abstract fun transferTo(position: Long, count: Long, target: WritableByteChannel): Long
    abstract fun transferFrom(src: ReadableByteChannel, position: Long, count: Long): Long
    abstract fun lock(position: Long, size: Long, shared: Boolean): FileLock
    fun lock(): FileLock
    /** [Nullability Decision] tryLock returns null if the lock cannot be acquired because another program holds an overlapping lock. */
    abstract fun tryLock(position: Long, size: Long, shared: Boolean): FileLock?
    fun tryLock(): FileLock?
    abstract override fun close()
    abstract override fun isOpen(): Boolean

    override val isOpen: Boolean
    val size: Long

    companion object {
        /**
         * [Nullability Decision] Java FileChannel.open(Path, OpenOption...) path is non-null;
         * option elements are treated as non-null (JDK throws NPE on null).
         */
        fun open(path: Path, vararg options: OpenOption): FileChannel
    }
}

/**
 * [Nullability Decision] Channels utility methods take non-null streams/channels and return non-null wrappers.
 */
expect class Channels {
    companion object {
        fun newInputStream(ch: ReadableByteChannel): InputStream
        fun newOutputStream(ch: WritableByteChannel): OutputStream
        fun newChannel(inStream: InputStream): ReadableByteChannel
        fun newChannel(out: OutputStream): WritableByteChannel
        fun newReader(ch: ReadableByteChannel, dec: CharsetDecoder, minBufferCap: Int): Reader
        fun newReader(ch: ReadableByteChannel, csName: String): Reader
        fun newWriter(ch: WritableByteChannel, enc: CharsetEncoder, minBufferCap: Int): Writer
        fun newWriter(ch: WritableByteChannel, csName: String): Writer
    }
}

/** [Nullability Decision] Java ClosedChannelException has only a non-null no-arg constructor. */
expect open class ClosedChannelException : IOException {
    constructor()
}

expect open class AsynchronousCloseException : ClosedChannelException {
    constructor()
}

expect open class ClosedByInterruptException : AsynchronousCloseException {
    constructor()
}

/** [Nullability Decision] Java NonReadableChannelException has only a non-null no-arg constructor. */
expect open class NonReadableChannelException : IllegalStateException {
    constructor()
}

/** [Nullability Decision] Java NonWritableChannelException has only a non-null no-arg constructor. */
expect open class NonWritableChannelException : IllegalStateException {
    constructor()
}

/**
 * A pair of channels that implements a unidirectional pipe.
 */
expect abstract class Pipe {
    protected constructor()

    abstract class SourceChannel : SelectableChannel, ReadableByteChannel, ScatteringByteChannel, InterruptibleChannel {
        override fun validOps(): Int
    }
    abstract class SinkChannel : SelectableChannel, WritableByteChannel, GatheringByteChannel, InterruptibleChannel {
        override fun validOps(): Int
    }

    abstract fun source(): SourceChannel
    abstract fun sink(): SinkChannel

    companion object {
        fun open(): Pipe
    }
}

expect open class AlreadyBoundException : IllegalStateException {
    constructor()
}

expect open class AlreadyConnectedException : IllegalStateException {
    constructor()
}

expect open class ConnectionPendingException : IllegalStateException {
    constructor()
}

expect open class NoConnectionPendingException : IllegalStateException {
    constructor()
}

expect open class NotYetBoundException : IllegalStateException {
    constructor()
}

expect open class NotYetConnectedException : IllegalStateException {
    constructor()
}

expect open class UnresolvedAddressException : IllegalArgumentException {
    constructor()
}

expect open class UnsupportedAddressTypeException : IllegalArgumentException {
    constructor()
}

expect open class CancelledKeyException : IllegalStateException {
    constructor()
}

expect open class ClosedSelectorException : IllegalStateException {
    constructor()
}

expect open class IllegalBlockingModeException : IllegalStateException {
    constructor()
}

expect open class IllegalSelectorException : IllegalArgumentException {
    constructor()
}

/**
 * [Nullability Decision] getLocalAddress returns null when the channel is unbound.
 */
expect interface NetworkChannel : Channel {
    fun bind(local: java.net.SocketAddress?): NetworkChannel
    fun getLocalAddress(): java.net.SocketAddress?
    fun <T> setOption(name: java.net.SocketOption<T>, value: T?): NetworkChannel
    fun <T> getOption(name: java.net.SocketOption<T>): T?
    fun supportedOptions(): Set<java.net.SocketOption<*>>
}

/**
 * [Nullability Decision] keyFor returns SelectionKey? (null if not registered with the selector);
 * register attachment is Any?.
 */
expect abstract class SelectableChannel : Channel, InterruptibleChannel {
    protected constructor()

    abstract fun validOps(): Int
    open fun isRegistered(): Boolean
    open fun keyFor(sel: Selector): SelectionKey?
    open fun register(sel: Selector, ops: Int, att: Any?): SelectionKey
    fun register(sel: Selector, ops: Int): SelectionKey
    open fun configureBlocking(block: Boolean): SelectableChannel
    open fun isBlocking(): Boolean
    open fun blockingLock(): Any
}

/**
 * [Nullability Decision] attach/attachment use Any? because attachments are optional.
 */
expect abstract class SelectionKey {
    protected constructor()

    abstract fun channel(): SelectableChannel
    abstract fun selector(): Selector
    abstract fun isValid(): Boolean
    abstract fun cancel()
    abstract fun interestOps(): Int
    abstract fun interestOps(ops: Int): SelectionKey
    abstract fun readyOps(): Int
    fun isReadable(): Boolean
    fun isWritable(): Boolean
    fun isConnectable(): Boolean
    fun isAcceptable(): Boolean
    fun attach(ob: Any?): Any?
    fun attachment(): Any?

    companion object {
        val OP_READ: Int
        val OP_WRITE: Int
        val OP_CONNECT: Int
        val OP_ACCEPT: Int
    }
}

expect abstract class Selector : Closeable {
    protected constructor()

    abstract fun isOpen(): Boolean
    abstract fun keys(): MutableSet<SelectionKey>
    abstract fun selectedKeys(): MutableSet<SelectionKey>
    abstract fun selectNow(): Int
    abstract fun select(timeout: Long): Int
    abstract fun select(): Int
    abstract fun wakeup(): Selector
    abstract override fun close()

    companion object {
        fun open(): Selector
    }
}

/**
 * [Nullability Decision] getRemoteAddress and getLocalAddress return SocketAddress? (null when unconnected/unbound).
 */
expect abstract class SocketChannel : SelectableChannel, ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {
    protected constructor()

    override fun validOps(): Int
    abstract override fun bind(local: java.net.SocketAddress?): SocketChannel
    abstract override fun <T> setOption(name: java.net.SocketOption<T>, value: T?): SocketChannel
    abstract fun shutdownInput(): SocketChannel
    abstract fun shutdownOutput(): SocketChannel
    abstract fun socket(): java.net.Socket
    abstract fun isConnected(): Boolean
    abstract fun isConnectionPending(): Boolean
    abstract fun connect(remote: java.net.SocketAddress): Boolean
    abstract fun finishConnect(): Boolean
    abstract fun getRemoteAddress(): java.net.SocketAddress?
    abstract override fun getLocalAddress(): java.net.SocketAddress?
    abstract override fun read(dst: ByteBuffer): Int
    abstract override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun read(dsts: Array<ByteBuffer>): Long
    abstract override fun write(src: ByteBuffer): Int
    abstract override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun write(srcs: Array<ByteBuffer>): Long

    companion object {
        fun open(): SocketChannel
        fun open(remote: java.net.SocketAddress): SocketChannel
    }
}

/**
 * [Nullability Decision] accept() returns SocketChannel? (null when in non-blocking mode and no connection is available).
 */
expect abstract class ServerSocketChannel : SelectableChannel, NetworkChannel {
    protected constructor()

    override fun validOps(): Int
    override fun bind(local: java.net.SocketAddress?): ServerSocketChannel
    abstract fun bind(local: java.net.SocketAddress?, backlog: Int): ServerSocketChannel
    abstract override fun <T> setOption(name: java.net.SocketOption<T>, value: T?): ServerSocketChannel
    abstract fun socket(): java.net.ServerSocket
    abstract fun accept(): SocketChannel?
    abstract override fun getLocalAddress(): java.net.SocketAddress?

    companion object {
        fun open(): ServerSocketChannel
    }
}

/**
 * [Nullability Decision] receive() returns SocketAddress? (null when in non-blocking mode and no datagram is available).
 */
expect abstract class DatagramChannel : SelectableChannel, ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel {
    protected constructor()

    override fun validOps(): Int
    abstract override fun bind(local: java.net.SocketAddress?): DatagramChannel
    abstract override fun <T> setOption(name: java.net.SocketOption<T>, value: T?): DatagramChannel
    abstract fun socket(): java.net.DatagramSocket
    abstract fun isConnected(): Boolean
    abstract fun connect(remote: java.net.SocketAddress): DatagramChannel
    abstract fun disconnect(): DatagramChannel
    abstract fun getRemoteAddress(): java.net.SocketAddress?
    abstract override fun getLocalAddress(): java.net.SocketAddress?
    abstract fun receive(dst: ByteBuffer): java.net.SocketAddress?
    abstract fun send(src: ByteBuffer, target: java.net.SocketAddress): Int
    abstract override fun read(dst: ByteBuffer): Int
    abstract override fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun read(dsts: Array<ByteBuffer>): Long
    abstract override fun write(src: ByteBuffer): Int
    abstract override fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    override fun write(srcs: Array<ByteBuffer>): Long

    companion object {
        fun open(): DatagramChannel
    }
}

