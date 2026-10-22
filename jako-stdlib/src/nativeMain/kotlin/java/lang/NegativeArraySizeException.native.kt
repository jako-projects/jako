package java.lang

actual open class NegativeArraySizeException : RuntimeException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
