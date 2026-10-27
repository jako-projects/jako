package java.nio.file.attribute

actual class FileTime private constructor(private val millis: Long) : Comparable<FileTime> {
    actual fun toMillis(): Long = millis

    actual override fun compareTo(other: FileTime): Int = millis.compareTo(other.millis)

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FileTime) return false
        return millis == other.millis
    }

    actual override fun hashCode(): Int = millis.hashCode()

    actual override fun toString(): String = millis.toString()

    actual companion object {
        actual fun fromMillis(value: Long): FileTime = FileTime(value)
    }
}
