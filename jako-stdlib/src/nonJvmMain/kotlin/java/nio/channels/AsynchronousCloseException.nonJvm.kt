package java.nio.channels

import kotlin.concurrent.withLock

actual open class AsynchronousCloseException actual constructor() : ClosedChannelException()
