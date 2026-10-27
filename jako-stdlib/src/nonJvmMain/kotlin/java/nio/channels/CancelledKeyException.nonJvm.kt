package java.nio.channels

import kotlin.concurrent.withLock

actual open class CancelledKeyException actual constructor() : IllegalStateException()
