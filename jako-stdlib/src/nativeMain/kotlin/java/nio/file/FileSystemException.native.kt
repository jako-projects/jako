package java.nio.file

import java.io.IOException
import java.nio.file.attribute.nativeReadBasicAttributes

actual open class FileSystemException actual constructor(
    actual val file: String?,
    other: String?,
    actual val reason: String?
) : IOException(buildMessage(file, other, reason)) {
    actual constructor(file: String?) : this(file, null, null)
    actual val otherFile: String? = other

    companion object {
        private fun buildMessage(file: String?, other: String?, reason: String?): String {
            val sb = StringBuilder()
            if (file != null) sb.append(file)
            if (other != null) sb.append(" -> ").append(other)
            if (reason != null) {
                if (sb.isNotEmpty()) sb.append(": ")
                sb.append(reason)
            }
            return sb.toString()
        }
    }
}
