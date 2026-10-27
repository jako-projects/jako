package java.net

actual open class NoRouteToHostException : SocketException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
