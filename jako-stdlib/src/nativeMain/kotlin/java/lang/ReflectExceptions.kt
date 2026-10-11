package java.lang

actual open class NoSuchFieldException : Exception {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual open class NoSuchMethodException : Exception {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

