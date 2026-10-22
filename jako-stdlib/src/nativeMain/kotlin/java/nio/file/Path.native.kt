package java.nio.file

import java.io.File
import java.net.URI

actual interface Path : Comparable<Path>, Iterable<Path> {
    actual fun getFileSystem(): FileSystem
    actual val fileSystem: FileSystem
    actual fun isAbsolute(): Boolean
    actual fun getRoot(): Path?
    actual fun getFileName(): Path?
    actual fun getParent(): Path?
    actual fun getNameCount(): Int
    actual fun getName(index: Int): Path
    actual fun subpath(beginIndex: Int, endIndex: Int): Path
    actual fun startsWith(other: Path): Boolean
    actual fun startsWith(other: String): Boolean
    actual fun endsWith(other: Path): Boolean
    actual fun endsWith(other: String): Boolean
    actual fun normalize(): Path
    actual fun resolve(other: Path): Path
    actual fun resolve(other: String): Path
    actual fun resolveSibling(other: Path): Path
    actual fun resolveSibling(other: String): Path
    actual fun relativize(other: Path): Path
    actual fun toUri(): URI
    actual fun toAbsolutePath(): Path
    actual fun toRealPath(vararg options: LinkOption): Path
    actual fun toFile(): File
    actual override fun iterator(): Iterator<Path>
    actual override fun compareTo(other: Path): Int
    actual override fun toString(): String
    actual override fun equals(other: Any?): Boolean
    actual override fun hashCode(): Int
}
