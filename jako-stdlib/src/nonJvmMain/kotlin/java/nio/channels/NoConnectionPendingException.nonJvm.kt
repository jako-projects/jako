package java.nio.channels

import kotlin.concurrent.withLock

actual open class NoConnectionPendingException actual constructor() : IllegalStateException()
