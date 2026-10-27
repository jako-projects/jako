package java.nio.channels

import kotlin.concurrent.withLock

actual interface InterruptibleChannel : Channel {
    actual override fun close()
}
