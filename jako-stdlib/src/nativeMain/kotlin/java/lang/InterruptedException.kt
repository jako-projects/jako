package java.lang

actual open class InterruptedException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
