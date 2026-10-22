@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.nio.ByteBuffer

expect interface SeekableByteChannel : ByteChannel {
    override fun read(dst: ByteBuffer): Int
    override fun write(src: ByteBuffer): Int
    fun position(): Long
    fun position(newPosition: Long): SeekableByteChannel
    fun size(): Long
    fun truncate(size: Long): SeekableByteChannel
}
