package java.nio.channels

import kotlin.concurrent.withLock

actual open class NonReadableChannelException actual constructor() : IllegalStateException()
