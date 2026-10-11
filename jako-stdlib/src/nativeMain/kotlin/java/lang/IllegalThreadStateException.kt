package java.lang

actual open class IllegalThreadStateException : IllegalArgumentException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
