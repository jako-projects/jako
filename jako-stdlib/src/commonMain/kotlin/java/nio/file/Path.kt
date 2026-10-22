@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

import java.io.File
import java.net.URI

expect interface Path : Comparable<Path>, Iterable<Path> {
    fun getFileSystem(): FileSystem
    val fileSystem: FileSystem
    fun isAbsolute(): Boolean
    fun getRoot(): Path?
    fun getFileName(): Path?
    fun getParent(): Path?
    fun getNameCount(): Int
    fun getName(index: Int): Path
    fun subpath(beginIndex: Int, endIndex: Int): Path
    fun startsWith(other: Path): Boolean
    fun startsWith(other: String): Boolean
    fun endsWith(other: Path): Boolean
    fun endsWith(other: String): Boolean
    fun normalize(): Path
    fun resolve(other: Path): Path
    fun resolve(other: String): Path
    fun resolveSibling(other: Path): Path
    fun resolveSibling(other: String): Path
    fun relativize(other: Path): Path
    fun toUri(): URI
    fun toAbsolutePath(): Path
    fun toRealPath(vararg options: LinkOption): Path
    fun toFile(): File
    override fun iterator(): Iterator<Path>
    override fun compareTo(other: Path): Int
    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
