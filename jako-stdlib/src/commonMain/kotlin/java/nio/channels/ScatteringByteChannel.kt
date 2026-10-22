@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.nio.ByteBuffer

expect interface ScatteringByteChannel : ReadableByteChannel {
    fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    fun read(dsts: Array<ByteBuffer>): Long
}
