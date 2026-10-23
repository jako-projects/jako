package java.net

actual open class PortUnreachableException : SocketException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
