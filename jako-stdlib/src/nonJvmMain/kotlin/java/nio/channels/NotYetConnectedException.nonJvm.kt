package java.nio.channels

import kotlin.concurrent.withLock

actual open class NotYetConnectedException actual constructor() : IllegalStateException()
