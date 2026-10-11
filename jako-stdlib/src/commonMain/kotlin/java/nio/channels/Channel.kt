@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.io.Closeable
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.file.OpenOption
import java.nio.file.Path

expect interface Channel : Closeable {
    fun isOpen(): Boolean
    override fun close()

    val isOpen: Boolean
}

expect interface ReadableByteChannel : Channel {
    /** [Nullability Decision] Java ReadableByteChannel.read(ByteBuffer) dst is non-null. */
    fun read(dst: ByteBuffer): Int
}

expect interface WritableByteChannel : Channel {
    /** [Nullability Decision] Java WritableByteChannel.write(ByteBuffer) src is non-null. */
    fun write(src: ByteBuffer): Int
}

expect abstract class FileChannel : ReadableByteChannel, WritableByteChannel {
    protected constructor()

    abstract override fun read(dst: ByteBuffer): Int
    abstract override fun write(src: ByteBuffer): Int
    abstract fun position(): Long
    abstract fun position(newPosition: Long): FileChannel
    abstract fun size(): Long
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

/** [Nullability Decision] Java ClosedChannelException has only a non-null no-arg constructor. */
expect open class ClosedChannelException : IOException {
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
