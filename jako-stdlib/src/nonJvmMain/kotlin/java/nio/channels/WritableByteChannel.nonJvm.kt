package java.nio.channels

import java.nio.ByteBuffer
import kotlin.concurrent.withLock

actual interface WritableByteChannel : Channel {
    actual fun write(src: ByteBuffer): Int
}
