package java.nio.channels

import java.nio.ByteBuffer
import kotlin.concurrent.withLock

actual interface ReadableByteChannel : Channel {
    actual fun read(dst: ByteBuffer): Int
}
