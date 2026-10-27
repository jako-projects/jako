package java.io

actual open class SyncFailedException : IOException {
    actual constructor(message: String?) : super(message)
}
