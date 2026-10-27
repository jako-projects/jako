package java.nio.channels

import kotlin.concurrent.withLock

actual open class NonWritableChannelException actual constructor() : IllegalStateException()
