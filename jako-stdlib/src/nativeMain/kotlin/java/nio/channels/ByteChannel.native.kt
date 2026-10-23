package java.nio.channels

import kotlin.concurrent.withLock

actual interface ByteChannel : ReadableByteChannel, WritableByteChannel
