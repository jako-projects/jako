package java.util.concurrent

import kotlin.concurrent.withLock

actual open class RejectedExecutionException : RuntimeException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}
