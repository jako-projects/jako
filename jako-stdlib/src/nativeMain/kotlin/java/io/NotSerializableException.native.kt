package java.io

actual open class NotSerializableException : ObjectStreamException {
    actual constructor(classname: String?) : super(classname)
    actual constructor() : super()
}
