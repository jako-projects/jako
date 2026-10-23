package java.nio.channels

import kotlin.concurrent.withLock

actual open class ClosedByInterruptException actual constructor() : AsynchronousCloseException()
