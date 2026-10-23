package java.security

actual open class InvalidParameterException : IllegalArgumentException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
