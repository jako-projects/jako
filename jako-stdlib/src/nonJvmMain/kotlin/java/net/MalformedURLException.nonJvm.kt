package java.net

import java.io.IOException

actual open class MalformedURLException : IOException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}
