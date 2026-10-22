package java.util.zip

import java.io.IOException

actual open class ZipException : IOException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
