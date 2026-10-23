@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.nio.ByteBuffer

expect interface ReadableByteChannel : Channel {
    /** [Nullability Decision] Java ReadableByteChannel.read(ByteBuffer) dst is non-null. */
    fun read(dst: ByteBuffer): Int
}
