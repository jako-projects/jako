package java.net

import java.io.IOException

actual open class ProtocolException : IOException {
    actual constructor() : super()
    actual constructor(host: String?) : super(host)
}
