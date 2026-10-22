package java.nio.channels

import java.nio.ByteBuffer
import kotlin.concurrent.withLock

actual interface SeekableByteChannel : ByteChannel {
    actual override fun read(dst: ByteBuffer): Int
    actual override fun write(src: ByteBuffer): Int
    actual fun position(): Long
    actual fun position(newPosition: Long): SeekableByteChannel
    actual fun size(): Long
    actual fun truncate(size: Long): SeekableByteChannel
}
