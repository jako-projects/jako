package java.net

actual open class BindException : SocketException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
