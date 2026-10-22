package java.lang

actual open class IllegalMonitorStateException : RuntimeException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
