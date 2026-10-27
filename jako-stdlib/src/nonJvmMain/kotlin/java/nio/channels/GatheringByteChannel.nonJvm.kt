package java.nio.channels

import java.nio.ByteBuffer
import kotlin.concurrent.withLock

actual interface GatheringByteChannel : WritableByteChannel {
    actual fun write(srcs: Array<ByteBuffer>, offset: Int, length: Int): Long
    actual fun write(srcs: Array<ByteBuffer>): Long
}
