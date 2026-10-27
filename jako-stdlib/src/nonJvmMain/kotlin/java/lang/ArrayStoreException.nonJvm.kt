package java.lang

actual open class ArrayStoreException : RuntimeException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
