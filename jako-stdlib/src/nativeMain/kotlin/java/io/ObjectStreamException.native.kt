package java.io

actual abstract class ObjectStreamException : IOException {
    actual protected constructor(classname: String?) : super(classname)
    actual protected constructor() : super()
}
