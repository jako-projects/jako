package java.nio.file.attribute

import java.io.File
import java.nio.file.NoSuchFileException
import java.nio.file.Path

actual class PosixFilePermissions private constructor() {
    actual companion object {
        actual fun toString(perms: Set<PosixFilePermission>): String {
            val sb = StringBuilder(9)
            sb.append(if (perms.contains(PosixFilePermission.OWNER_READ)) 'r' else '-')
            sb.append(if (perms.contains(PosixFilePermission.OWNER_WRITE)) 'w' else '-')
            sb.append(if (perms.contains(PosixFilePermission.OWNER_EXECUTE)) 'x' else '-')
            sb.append(if (perms.contains(PosixFilePermission.GROUP_READ)) 'r' else '-')
            sb.append(if (perms.contains(PosixFilePermission.GROUP_WRITE)) 'w' else '-')
            sb.append(if (perms.contains(PosixFilePermission.GROUP_EXECUTE)) 'x' else '-')
            sb.append(if (perms.contains(PosixFilePermission.OTHERS_READ)) 'r' else '-')
            sb.append(if (perms.contains(PosixFilePermission.OTHERS_WRITE)) 'w' else '-')
            sb.append(if (perms.contains(PosixFilePermission.OTHERS_EXECUTE)) 'x' else '-')
            return sb.toString()
        }

        actual fun fromString(perms: String): MutableSet<PosixFilePermission> {
            if (perms.length != 9) {
                throw IllegalArgumentException("Invalid mode: $perms")
            }
            val result = LinkedHashSet<PosixFilePermission>()
            if (isR(perms[0])) result.add(PosixFilePermission.OWNER_READ)
            if (isW(perms[1])) result.add(PosixFilePermission.OWNER_WRITE)
            if (isX(perms[2])) result.add(PosixFilePermission.OWNER_EXECUTE)
            if (isR(perms[3])) result.add(PosixFilePermission.GROUP_READ)
            if (isW(perms[4])) result.add(PosixFilePermission.GROUP_WRITE)
            if (isX(perms[5])) result.add(PosixFilePermission.GROUP_EXECUTE)
            if (isR(perms[6])) result.add(PosixFilePermission.OTHERS_READ)
            if (isW(perms[7])) result.add(PosixFilePermission.OTHERS_WRITE)
            if (isX(perms[8])) result.add(PosixFilePermission.OTHERS_EXECUTE)
            return result
        }

        actual fun asFileAttribute(perms: Set<PosixFilePermission>): FileAttribute<Set<PosixFilePermission>> {
            val copy = LinkedHashSet<PosixFilePermission>()
            for (p in perms) {
                copy.add(p)
            }
            return object : FileAttribute<Set<PosixFilePermission>> {
                override fun name(): String = "posix:permissions"
                override fun value(): Set<PosixFilePermission> = java.util.Collections.unmodifiableSet(copy)
            }
        }

        private fun isR(c: Char): Boolean = when (c) {
            'r' -> true
            '-' -> false
            else -> throw IllegalArgumentException("Invalid permission character: $c")
        }

        private fun isW(c: Char): Boolean = when (c) {
            'w' -> true
            '-' -> false
            else -> throw IllegalArgumentException("Invalid permission character: $c")
        }

        private fun isX(c: Char): Boolean = when (c) {
            'x' -> true
            '-' -> false
            else -> throw IllegalArgumentException("Invalid permission character: $c")
        }

        internal fun modeToPermissions(mode: Int): MutableSet<PosixFilePermission> {
            val res = LinkedHashSet<PosixFilePermission>()
            if ((mode and 0x100) != 0) res.add(PosixFilePermission.OWNER_READ)
            if ((mode and 0x080) != 0) res.add(PosixFilePermission.OWNER_WRITE)
            if ((mode and 0x040) != 0) res.add(PosixFilePermission.OWNER_EXECUTE)
            if ((mode and 0x020) != 0) res.add(PosixFilePermission.GROUP_READ)
            if ((mode and 0x010) != 0) res.add(PosixFilePermission.GROUP_WRITE)
            if ((mode and 0x008) != 0) res.add(PosixFilePermission.GROUP_EXECUTE)
            if ((mode and 0x004) != 0) res.add(PosixFilePermission.OTHERS_READ)
            if ((mode and 0x002) != 0) res.add(PosixFilePermission.OTHERS_WRITE)
            if ((mode and 0x001) != 0) res.add(PosixFilePermission.OTHERS_EXECUTE)
            return res
        }

        internal fun permissionsToMode(perms: Set<PosixFilePermission>): Int {
            var mode = 0
            if (perms.contains(PosixFilePermission.OWNER_READ)) mode = mode or 0x100
            if (perms.contains(PosixFilePermission.OWNER_WRITE)) mode = mode or 0x080
            if (perms.contains(PosixFilePermission.OWNER_EXECUTE)) mode = mode or 0x040
            if (perms.contains(PosixFilePermission.GROUP_READ)) mode = mode or 0x020
            if (perms.contains(PosixFilePermission.GROUP_WRITE)) mode = mode or 0x010
            if (perms.contains(PosixFilePermission.GROUP_EXECUTE)) mode = mode or 0x008
            if (perms.contains(PosixFilePermission.OTHERS_READ)) mode = mode or 0x004
            if (perms.contains(PosixFilePermission.OTHERS_WRITE)) mode = mode or 0x002
            if (perms.contains(PosixFilePermission.OTHERS_EXECUTE)) mode = mode or 0x001
            return mode
        }
    }
}

internal class NativeBasicFileAttributes(
    private val lastModified: FileTime,
    private val directory: Boolean,
    private val regularFile: Boolean,
    private val symbolicLink: Boolean,
    private val fileSize: Long,
    private val key: Any?
) : BasicFileAttributes {
    override fun lastModifiedTime(): FileTime = lastModified
    override fun lastAccessTime(): FileTime = lastModified
    override fun creationTime(): FileTime = lastModified
    override fun isRegularFile(): Boolean = regularFile
    override fun isDirectory(): Boolean = directory
    override fun isSymbolicLink(): Boolean = symbolicLink
    override fun isOther(): Boolean = !directory && !regularFile && !symbolicLink
    override fun size(): Long = fileSize
    override fun fileKey(): Any? = key

    override val lastModifiedTime: FileTime get() = lastModifiedTime()
    override val lastAccessTime: FileTime get() = lastAccessTime()
    override val creationTime: FileTime get() = creationTime()
    override val isRegularFile: Boolean get() = isRegularFile()
    override val isDirectory: Boolean get() = isDirectory()
    override val isSymbolicLink: Boolean get() = isSymbolicLink()
    override val isOther: Boolean get() = isOther()
    override val size: Long get() = size()
}

internal fun nativeReadBasicAttributes(path: Path): BasicFileAttributes {
    val file = path.toFile()
    if (!file.exists()) throw NoSuchFileException(path.toString())
    val directory = file.isDirectory()
    val regularFile = file.isFile()
    return NativeBasicFileAttributes(
        lastModified = FileTime.fromMillis(fileLastModifiedMillis(file)),
        directory = directory,
        regularFile = regularFile,
        symbolicLink = false,
        fileSize = if (regularFile || directory) file.length() else 0L,
        key = path.toAbsolutePath().toString()
    )
}

internal fun fileLastModifiedMillis(file: File): Long = file.lastModified()
