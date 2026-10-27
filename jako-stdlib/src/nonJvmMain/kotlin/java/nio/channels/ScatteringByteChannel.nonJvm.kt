package java.nio.channels

import java.nio.ByteBuffer
import kotlin.concurrent.withLock

actual interface ScatteringByteChannel : ReadableByteChannel {
    actual fun read(dsts: Array<ByteBuffer>, offset: Int, length: Int): Long
    actual fun read(dsts: Array<ByteBuffer>): Long
}
