@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file.attribute

expect class FileTime : Comparable<FileTime> {
    fun toMillis(): Long
    override fun compareTo(other: FileTime): Int
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun toString(): String

    companion object {
        fun fromMillis(value: Long): FileTime
    }
}
