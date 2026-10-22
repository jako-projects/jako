@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.nio.ByteBuffer

expect interface WritableByteChannel : Channel {
    /** [Nullability Decision] Java WritableByteChannel.write(ByteBuffer) src is non-null. */
    fun write(src: ByteBuffer): Int
}
