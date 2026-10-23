package java.io

actual open class InvalidObjectException : ObjectStreamException {
    actual constructor(reason: String?) : super(reason)
}
