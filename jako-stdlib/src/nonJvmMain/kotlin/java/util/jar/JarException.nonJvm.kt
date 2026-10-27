package java.util.jar

import java.util.zip.ZipException

actual open class JarException : ZipException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
