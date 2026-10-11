package java.io

actual open class IOException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

actual open class FileNotFoundException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class EOFException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class InterruptedIOException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}

actual open class SyncFailedException : IOException {
    actual constructor(message: String?) : super(message)
}
