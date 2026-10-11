@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.net.URI
import java.nio.charset.Charset
import java.nio.file.attribute.FileTime

expect interface OpenOption

expect enum class StandardOpenOption : OpenOption {
    READ,
    WRITE,
    APPEND,
    TRUNCATE_EXISTING,
    CREATE,
    CREATE_NEW,
    DELETE_ON_CLOSE,
    SPARSE,
    SYNC,
    DSYNC
}

expect interface CopyOption

expect enum class StandardCopyOption : CopyOption {
    REPLACE_EXISTING,
    COPY_ATTRIBUTES,
    ATOMIC_MOVE
}

expect enum class LinkOption : OpenOption, CopyOption {
    NOFOLLOW_LINKS
}

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

expect class Paths {
    companion object {
        fun get(first: String, vararg more: String): Path
        fun get(uri: URI): Path
    }
}

expect class Files {
    companion object {
        fun exists(path: Path, vararg options: LinkOption): Boolean
        fun notExists(path: Path, vararg options: LinkOption): Boolean
        fun isDirectory(path: Path, vararg options: LinkOption): Boolean
        fun isRegularFile(path: Path, vararg options: LinkOption): Boolean
        fun isReadable(path: Path): Boolean
        fun isWritable(path: Path): Boolean
        fun isExecutable(path: Path): Boolean
        fun size(path: Path): Long
        fun createFile(path: Path): Path
        fun createDirectory(path: Path): Path
        fun createDirectories(path: Path): Path
        fun delete(path: Path)
        fun deleteIfExists(path: Path): Boolean
        fun copy(source: Path, target: Path, vararg options: CopyOption): Path
        fun copy(inStream: InputStream, target: Path, vararg options: CopyOption): Long
        fun copy(source: Path, out: OutputStream): Long
        fun move(source: Path, target: Path, vararg options: CopyOption): Path
        fun newInputStream(path: Path, vararg options: OpenOption): InputStream
        fun newOutputStream(path: Path, vararg options: OpenOption): OutputStream
        fun newBufferedReader(path: Path, cs: Charset): BufferedReader
        fun newBufferedReader(path: Path): BufferedReader
        fun newBufferedWriter(path: Path, cs: Charset, vararg options: OpenOption): BufferedWriter
        fun newBufferedWriter(path: Path, vararg options: OpenOption): BufferedWriter
        fun readAllBytes(path: Path): ByteArray
        fun readAllLines(path: Path, cs: Charset): List<String>
        fun readAllLines(path: Path): List<String>
        fun write(path: Path, bytes: ByteArray, vararg options: OpenOption): Path
        fun write(path: Path, lines: Iterable<CharSequence>, cs: Charset, vararg options: OpenOption): Path
        fun write(path: Path, lines: Iterable<CharSequence>, vararg options: OpenOption): Path
        fun walkFileTree(start: Path, visitor: FileVisitor<in Path>): Path
        fun walkFileTree(start: Path, options: Set<FileVisitOption>, maxDepth: Int, visitor: FileVisitor<in Path>): Path
        fun readAttributes(path: Path, attributes: String, vararg options: LinkOption): MutableMap<String, Any>
        fun getLastModifiedTime(path: Path, vararg options: LinkOption): FileTime
        fun isSymbolicLink(path: Path): Boolean
    }
}

expect open class FileSystemException : java.io.IOException {
    constructor(file: String?)
    constructor(file: String?, other: String?, reason: String?)
    val file: String?
    val otherFile: String?
    val reason: String?
}

expect open class NoSuchFileException : FileSystemException {
    constructor(file: String?)
    constructor(file: String?, other: String?, reason: String?)
}

expect open class FileAlreadyExistsException : FileSystemException {
    constructor(file: String?)
    constructor(file: String?, other: String?, reason: String?)
}
