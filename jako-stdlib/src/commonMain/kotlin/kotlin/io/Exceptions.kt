package kotlin.io

import java.io.File
import java.io.IOException

public open class FileSystemException(
    public val file: File,
    public val other: File? = null,
    public val reason: String? = null
) : IOException(buildMessage(file, other, reason))

private fun buildMessage(file: File, other: File?, reason: String?): String {
    val sb = StringBuilder(file.toString())
    if (other != null) {
        sb.append(" -> $other")
    }
    if (reason != null) {
        sb.append(": $reason")
    }
    return sb.toString()
}

public class FileAlreadyExistsException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)

public class NoSuchFileException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)
