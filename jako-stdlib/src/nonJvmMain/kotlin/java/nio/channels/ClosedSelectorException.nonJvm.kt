package java.nio.channels

import kotlin.concurrent.withLock

actual open class ClosedSelectorException actual constructor() : IllegalStateException()
