package java.net

import java.io.IOException

actual open class SocketException : IOException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
