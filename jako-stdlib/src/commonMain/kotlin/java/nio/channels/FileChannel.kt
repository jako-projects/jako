@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.nio.ByteBuffer
import java.nio.file.OpenOption
import java.nio.file.Path

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
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
