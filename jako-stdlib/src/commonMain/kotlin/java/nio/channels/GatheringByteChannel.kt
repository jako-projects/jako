@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.nio.ByteBuffer

expect interface GatheringByteChannel : WritableByteChannel {
    fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    fun write(srcs: Array<ByteBuffer>): Long
}
