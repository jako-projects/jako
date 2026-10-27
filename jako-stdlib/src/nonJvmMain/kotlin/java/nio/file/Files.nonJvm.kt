package java.nio.file

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.nio.file.attribute.FileAttribute
import java.nio.file.attribute.FileTime
import java.nio.file.attribute.nativeReadBasicAttributes

internal class NativeDirectoryStream(
    private val entries: MutableList<Path>
) : DirectoryStream<Path> {
    private var closed = false
    private var iteratorObtained = false

    override fun iterator(): MutableIterator<Path> {
        if (closed) throw IllegalStateException("Directory stream is closed")
        if (iteratorObtained) throw IllegalStateException("Iterator already obtained")
        iteratorObtained = true
        val backing = entries.iterator()
        return object : MutableIterator<Path> {
            override fun hasNext(): Boolean = !closed && backing.hasNext()
            override fun next(): Path {
                if (closed) throw NoSuchElementException()
                return backing.next()
            }
            override fun remove() {
                throw UnsupportedOperationException("remove")
            }
        }
    }

    override fun close() {
        closed = true
    }
}

actual class Files {
    actual companion object {
        actual fun exists(path: Path, vararg options: LinkOption): Boolean = path.toFile().exists()

        actual fun notExists(path: Path, vararg options: LinkOption): Boolean = !exists(path, *options)

        actual fun isDirectory(path: Path, vararg options: LinkOption): Boolean = path.toFile().isDirectory()

        actual fun isRegularFile(path: Path, vararg options: LinkOption): Boolean = path.toFile().isFile()

        actual fun isReadable(path: Path): Boolean = path.toFile().canRead()

        actual fun isWritable(path: Path): Boolean = path.toFile().canWrite()

        actual fun isExecutable(path: Path): Boolean = path.toFile().canExecute()

        actual fun isSameFile(path: Path, path2: Path): Boolean {
            if (path == path2) return true
            val f1 = path.toFile()
            val f2 = path2.toFile()
            if (!f1.exists()) throw NoSuchFileException(path.toString())
            if (!f2.exists()) throw NoSuchFileException(path2.toString())
            return f1.getCanonicalPath() == f2.getCanonicalPath()
        }

        actual fun isHidden(path: Path): Boolean = path.toFile().isHidden()

        actual fun size(path: Path): Long {
            val file = path.toFile()
            if (!file.exists()) throw NoSuchFileException(path.toString())
            return file.length()
        }

        private fun applyFileAttributes(path: Path, attrs: Array<out FileAttribute<*>>) {
            for (attr in attrs) {
                if (attr.name() == "posix:permissions") {
                    @Suppress("UNCHECKED_CAST")
                    val perms = attr.value() as? Set<java.nio.file.attribute.PosixFilePermission>
                    if (perms != null) {
                        setPosixFilePermissions(path, perms)
                    }
                }
            }
        }

        actual fun createFile(path: Path, vararg attrs: FileAttribute<*>): Path {
            val file = path.toFile()
            if (file.exists()) throw FileAlreadyExistsException(path.toString())
            if (!file.createNewFile()) throw FileAlreadyExistsException(path.toString())
            applyFileAttributes(path, attrs)
            return path
        }

        actual fun createDirectory(path: Path, vararg attrs: FileAttribute<*>): Path {
            val file = path.toFile()
            if (file.exists()) throw FileAlreadyExistsException(path.toString())
            if (!file.mkdir()) throw IOException("Failed to create directory $path")
            applyFileAttributes(path, attrs)
            return path
        }

        actual fun createDirectories(path: Path, vararg attrs: FileAttribute<*>): Path {
            val file = path.toFile()
            file.mkdirs()
            applyFileAttributes(path, attrs)
            return path
        }

        private fun checkTempPrefixSuffix(prefix: String?, suffix: String?) {
            if (prefix != null && (prefix.contains('/') || prefix.contains('\\'))) {
                throw IllegalArgumentException("Invalid prefix: $prefix")
            }
            if (suffix != null && (suffix.contains('/') || suffix.contains('\\'))) {
                throw IllegalArgumentException("Invalid suffix: $suffix")
            }
        }

        actual fun createTempFile(
            dir: Path,
            prefix: String?,
            suffix: String?,
            vararg attrs: FileAttribute<*>
        ): Path {
            checkTempPrefixSuffix(prefix, suffix)
            val dirFile = dir.toFile()
            if (!dirFile.exists()) throw NoSuchFileException(dir.toString())
            if (!dirFile.isDirectory()) throw NotDirectoryException(dir.toString())
            val pfx = prefix ?: ""
            val sfx = suffix ?: ".tmp"
            var attempts = 0
            while (attempts < 10000) {
                attempts++
                val randomNum = kotlin.random.Random.nextLong().let { if (it < 0) -it else it }
                val candidate = dir.resolve("$pfx$randomNum$sfx")
                if (candidate.toFile().createNewFile()) {
                    return candidate
                }
            }
            throw IOException("Unable to create temporary file in $dir")
        }

        actual fun createTempFile(
            prefix: String?,
            suffix: String?,
            vararg attrs: FileAttribute<*>
        ): Path {
            val tmpDir = Paths.get(java.lang.System.getProperty("java.io.tmpdir") ?: "/tmp")
            return createTempFile(tmpDir, prefix, suffix, *attrs)
        }

        actual fun createTempDirectory(
            dir: Path,
            prefix: String?,
            vararg attrs: FileAttribute<*>
        ): Path {
            checkTempPrefixSuffix(prefix, null)
            val dirFile = dir.toFile()
            if (!dirFile.exists()) throw NoSuchFileException(dir.toString())
            if (!dirFile.isDirectory()) throw NotDirectoryException(dir.toString())
            val pfx = prefix ?: ""
            var attempts = 0
            while (attempts < 10000) {
                attempts++
                val randomNum = kotlin.random.Random.nextLong().let { if (it < 0) -it else it }
                val candidate = dir.resolve("$pfx$randomNum")
                if (candidate.toFile().mkdir()) {
                    return candidate
                }
            }
            throw IOException("Unable to create temporary directory in $dir")
        }

        actual fun createTempDirectory(
            prefix: String?,
            vararg attrs: FileAttribute<*>
        ): Path {
            val tmpDir = Paths.get(java.lang.System.getProperty("java.io.tmpdir") ?: "/tmp")
            return createTempDirectory(tmpDir, prefix, *attrs)
        }

        actual fun newDirectoryStream(dir: Path): DirectoryStream<Path> {
            return newDirectoryStream(dir, DirectoryStream.Filter { true })
        }

        actual fun newDirectoryStream(dir: Path, glob: String): DirectoryStream<Path> {
            if (glob == "*") return newDirectoryStream(dir)
            val matcher = dir.getFileSystem().getPathMatcher("glob:$glob")
            return newDirectoryStream(dir, DirectoryStream.Filter { entry ->
                val name = entry.getFileName()
                name != null && matcher.matches(name)
            })
        }

        actual fun newDirectoryStream(
            dir: Path,
            filter: DirectoryStream.Filter<in Path>
        ): DirectoryStream<Path> {
            val dirFile = dir.toFile()
            if (!dirFile.exists()) throw NoSuchFileException(dir.toString())
            if (!dirFile.isDirectory()) throw NotDirectoryException(dir.toString())
            val names = dirFile.list() ?: throw IOException("Failed to list directory: $dir")
            val entries = mutableListOf<Path>()
            for (name in names) {
                val entry = dir.resolve(name)
                if (filter.accept(entry)) {
                    entries.add(entry)
                }
            }
            return NativeDirectoryStream(entries)
        }

        actual fun delete(path: Path) {
            val file = path.toFile()
            if (!file.exists()) throw NoSuchFileException(path.toString())
            if (file.isDirectory() && (file.list()?.isNotEmpty() == true)) {
                throw DirectoryNotEmptyException(path.toString())
            }
            if (!file.delete()) throw IOException("Failed to delete $path")
        }

        actual fun deleteIfExists(path: Path): Boolean {
            val file = path.toFile()
            if (!file.exists()) return false
            if (file.isDirectory() && (file.list()?.isNotEmpty() == true)) {
                throw DirectoryNotEmptyException(path.toString())
            }
            return file.delete()
        }

        actual fun copy(source: Path, target: Path, vararg options: CopyOption): Path {
            val inStream = newInputStream(source)
            try {
                copy(inStream, target, *options)
            } finally {
                inStream.close()
            }
            return target
        }

        actual fun copy(inStream: InputStream, target: Path, vararg options: CopyOption): Long {
            val replace = options.contains(StandardCopyOption.REPLACE_EXISTING)
            val file = target.toFile()
            if (file.exists() && !replace) {
                throw FileAlreadyExistsException(target.toString())
            }
            val out = FileOutputStream(file)
            var total = 0L
            try {
                val buf = ByteArray(8192)
                var r = inStream.read(buf)
                while (r != -1) {
                    out.write(buf, 0, r)
                    total += r
                    r = inStream.read(buf)
                }
            } finally {
                out.close()
            }
            return total
        }

        actual fun copy(source: Path, out: OutputStream): Long {
            val inStream = newInputStream(source)
            var total = 0L
            try {
                val buf = ByteArray(8192)
                var r = inStream.read(buf)
                while (r != -1) {
                    out.write(buf, 0, r)
                    total += r
                    r = inStream.read(buf)
                }
            } finally {
                inStream.close()
            }
            return total
        }

        actual fun move(source: Path, target: Path, vararg options: CopyOption): Path {
            copy(source, target, *options)
            delete(source)
            return target
        }

        actual fun newInputStream(path: Path, vararg options: OpenOption): InputStream {
            val file = path.toFile()
            if (!file.exists()) throw NoSuchFileException(path.toString())
            return FileInputStream(file)
        }

        actual fun newOutputStream(path: Path, vararg options: OpenOption): OutputStream {
            val file = path.toFile()
            val append = options.contains(StandardOpenOption.APPEND)
            val createNew = options.contains(StandardOpenOption.CREATE_NEW)
            if (createNew && file.exists()) {
                throw FileAlreadyExistsException(path.toString())
            }
            return FileOutputStream(file, append)
        }

        actual fun newBufferedReader(path: Path, cs: Charset): BufferedReader {
            return BufferedReader(InputStreamReader(newInputStream(path), cs))
        }

        actual fun newBufferedReader(path: Path): BufferedReader {
            return newBufferedReader(path, StandardCharsets.UTF_8)
        }

        actual fun newBufferedWriter(path: Path, cs: Charset, vararg options: OpenOption): BufferedWriter {
            return BufferedWriter(OutputStreamWriter(newOutputStream(path, *options), cs))
        }

        actual fun newBufferedWriter(path: Path, vararg options: OpenOption): BufferedWriter {
            return newBufferedWriter(path, StandardCharsets.UTF_8, *options)
        }

        actual fun readAllBytes(path: Path): ByteArray {
            val inStream = newInputStream(path)
            val baos = ByteArrayOutputStream()
            try {
                val buf = ByteArray(8192)
                var r = inStream.read(buf)
                while (r != -1) {
                    baos.write(buf, 0, r)
                    r = inStream.read(buf)
                }
            } finally {
                inStream.close()
            }
            return baos.toByteArray()
        }

        actual fun readAllLines(path: Path, cs: Charset): List<String> {
            val reader = newBufferedReader(path, cs)
            val list = mutableListOf<String>()
            try {
                var line = reader.readLine()
                while (line != null) {
                    list.add(line)
                    line = reader.readLine()
                }
            } finally {
                reader.close()
            }
            return list
        }

        actual fun readAllLines(path: Path): List<String> = readAllLines(path, StandardCharsets.UTF_8)

        actual fun write(path: Path, bytes: ByteArray, vararg options: OpenOption): Path {
            val out = newOutputStream(path, *options)
            try {
                out.write(bytes)
            } finally {
                out.close()
            }
            return path
        }

        actual fun write(path: Path, lines: Iterable<CharSequence>, cs: Charset, vararg options: OpenOption): Path {
            val writer = newBufferedWriter(path, cs, *options)
            try {
                for (l in lines) {
                    writer.write(l.toString())
                    writer.newLine()
                }
            } finally {
                writer.close()
            }
            return path
        }

        actual fun write(path: Path, lines: Iterable<CharSequence>, vararg options: OpenOption): Path {
            return write(path, lines, StandardCharsets.UTF_8, *options)
        }

        actual fun walkFileTree(start: Path, visitor: FileVisitor<in Path>): Path {
            return walkFileTree(start, emptySet(), Int.MAX_VALUE, visitor)
        }

        actual fun walkFileTree(
            start: Path,
            options: Set<FileVisitOption>,
            maxDepth: Int,
            visitor: FileVisitor<in Path>
        ): Path {
            return walkFileTreeImpl(start, maxDepth, visitor)
        }

        actual fun readAttributes(
            path: Path,
            attributes: String,
            vararg options: LinkOption
        ): MutableMap<String, Any> {
            val attrs = nativeReadBasicAttributes(path)
            val requested = parseBasicAttributeNames(attributes)
            val result = linkedMapOf<String, Any>()
            for (name in requested) {
                val value: Any = when (name) {
                    "size" -> attrs.size()
                    "lastModifiedTime" -> attrs.lastModifiedTime()
                    "lastAccessTime" -> attrs.lastAccessTime()
                    "creationTime" -> attrs.creationTime()
                    "isDirectory" -> attrs.isDirectory()
                    "isRegularFile" -> attrs.isRegularFile()
                    "isSymbolicLink" -> attrs.isSymbolicLink()
                    "isOther" -> attrs.isOther()
                    "fileKey" -> attrs.fileKey() ?: path.toString()
                    else -> throw IllegalArgumentException("Unknown attribute: $name")
                }
                result[name] = value
            }
            return result
        }

        actual fun getLastModifiedTime(path: Path, vararg options: LinkOption): FileTime {
            return nativeReadBasicAttributes(path).lastModifiedTime()
        }

        actual fun isSymbolicLink(path: Path): Boolean {
            return if (exists(path)) nativeReadBasicAttributes(path).isSymbolicLink() else false
        }

        actual fun getPosixFilePermissions(
            path: Path,
            vararg options: LinkOption
        ): MutableSet<java.nio.file.attribute.PosixFilePermission> {
            val file = path.toFile()
            if (!file.exists()) throw NoSuchFileException(path.toString())
            val mode = java.io.nativeGetPosixMode(file.getPath())
            if (mode < 0) throw IOException("Failed to read POSIX permissions: $path")
            return java.nio.file.attribute.PosixFilePermissions.modeToPermissions(mode)
        }

        actual fun setPosixFilePermissions(
            path: Path,
            perms: Set<java.nio.file.attribute.PosixFilePermission>
        ): Path {
            val file = path.toFile()
            if (!file.exists()) throw NoSuchFileException(path.toString())
            val mode = java.nio.file.attribute.PosixFilePermissions.permissionsToMode(perms)
            if (!java.io.nativeSetPosixMode(file.getPath(), mode)) {
                throw IOException("Failed to set POSIX permissions: $path")
            }
            return path
        }
    }
}

internal val ALL_BASIC_ATTRIBUTES = listOf(
    "lastModifiedTime",
    "lastAccessTime",
    "creationTime",
    "isRegularFile",
    "isDirectory",
    "isSymbolicLink",
    "isOther",
    "size",
    "fileKey"
)

internal fun parseBasicAttributeNames(attributes: String): List<String> {
    val colon = attributes.indexOf(':')
    val view = if (colon >= 0) attributes.substring(0, colon) else "basic"
    if (view.isNotEmpty() && view != "basic") {
        throw UnsupportedOperationException("View '$view' is not supported")
    }
    val list = if (colon >= 0) attributes.substring(colon + 1) else attributes
    if (list == "*" || list.isEmpty()) return ALL_BASIC_ATTRIBUTES
    return list.split(',').map { it.trim() }.filter { it.isNotEmpty() }
}
