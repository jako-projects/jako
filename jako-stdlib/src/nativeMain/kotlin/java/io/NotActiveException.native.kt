package java.io

actual open class NotActiveException : ObjectStreamException {
    actual constructor(reason: String?) : super(reason)
    actual constructor() : super()
}
