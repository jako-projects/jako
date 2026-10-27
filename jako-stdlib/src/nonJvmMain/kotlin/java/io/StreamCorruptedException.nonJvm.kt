package java.io

actual open class StreamCorruptedException : ObjectStreamException {
    actual constructor(reason: String?) : super(reason)
    actual constructor() : super()
}
