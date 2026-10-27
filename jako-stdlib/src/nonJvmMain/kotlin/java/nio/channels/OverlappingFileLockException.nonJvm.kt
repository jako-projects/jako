package java.nio.channels

import kotlin.concurrent.withLock

actual open class OverlappingFileLockException actual constructor() : IllegalStateException()
