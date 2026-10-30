@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileTime

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
expect class Files {
    companion object {
        fun exists(path: Path, vararg options: LinkOption): Boolean
        fun notExists(path: Path, vararg options: LinkOption): Boolean
        fun isDirectory(path: Path, vararg options: LinkOption): Boolean
        fun isRegularFile(path: Path, vararg options: LinkOption): Boolean
        fun isReadable(path: Path): Boolean
        fun isWritable(path: Path): Boolean
        fun isExecutable(path: Path): Boolean
        fun isSameFile(path: Path, path2: Path): Boolean
        fun isHidden(path: Path): Boolean
        fun size(path: Path): Long
        fun createFile(path: Path, vararg attrs: FileAttribute<*>): Path
        fun createDirectory(path: Path, vararg attrs: FileAttribute<*>): Path
        fun createDirectories(path: Path, vararg attrs: FileAttribute<*>): Path
        fun createTempFile(dir: Path, prefix: String?, suffix: String?, vararg attrs: FileAttribute<*>): Path
        fun createTempFile(prefix: String?, suffix: String?, vararg attrs: FileAttribute<*>): Path
        fun createTempDirectory(dir: Path, prefix: String?, vararg attrs: FileAttribute<*>): Path
        fun createTempDirectory(prefix: String?, vararg attrs: FileAttribute<*>): Path
        fun newDirectoryStream(dir: Path): DirectoryStream<Path>
        fun newDirectoryStream(dir: Path, glob: String): DirectoryStream<Path>
        fun newDirectoryStream(dir: Path, filter: DirectoryStream.Filter<in Path>): DirectoryStream<Path>
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
        fun getPosixFilePermissions(path: Path, vararg options: LinkOption): MutableSet<java.nio.file.attribute.PosixFilePermission>
        fun setPosixFilePermissions(path: Path, perms: Set<java.nio.file.attribute.PosixFilePermission>): Path
    }
}
