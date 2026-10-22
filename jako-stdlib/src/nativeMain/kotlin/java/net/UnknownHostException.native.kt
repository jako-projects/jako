package java.net

import java.io.IOException

actual open class UnknownHostException : IOException {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
