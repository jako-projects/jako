package java.lang

actual open class CloneNotSupportedException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
