package java.nio.channels

import java.io.Closeable
import kotlin.concurrent.withLock

actual interface Channel : Closeable {
    actual fun isOpen(): Boolean
    actual override fun close()
    actual val isOpen: Boolean
}
