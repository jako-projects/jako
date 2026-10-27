package java.nio.channels

import kotlin.concurrent.withLock

actual open class ConnectionPendingException actual constructor() : IllegalStateException()
