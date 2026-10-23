package java.nio.channels

import java.io.IOException
import kotlin.concurrent.withLock

actual open class ClosedChannelException actual constructor() : IOException()
