package java.net

actual open class ConnectException : SocketException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
