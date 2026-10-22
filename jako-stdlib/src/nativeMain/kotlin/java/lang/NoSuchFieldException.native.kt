package java.lang

actual open class NoSuchFieldException : Exception {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
