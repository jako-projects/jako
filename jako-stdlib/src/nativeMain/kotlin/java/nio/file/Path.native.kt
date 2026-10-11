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

internal class NativePath(private val pathString: String) : Path {
    private val normalizedPath: String = pathString.replace('\\', '/')

    override fun getFileSystem(): FileSystem = FileSystems.getDefault()

    override val fileSystem: FileSystem get() = getFileSystem()

    override fun isAbsolute(): Boolean {
        return normalizedPath.startsWith("/") ||
                (normalizedPath.length >= 3 && normalizedPath[1] == ':' && normalizedPath[2] == '/')
    }

    override fun getRoot(): Path? {
        if (!isAbsolute()) return null
        return if (normalizedPath.startsWith("/")) {
            NativePath("/")
        } else {
            NativePath(normalizedPath.substring(0, 3))
        }
    }

    private fun getComponents(): List<String> {
        val withoutRoot = if (isAbsolute()) {
            val root = getRoot()?.toString() ?: ""
            normalizedPath.substring(root.length)
        } else {
            normalizedPath
        }
        return withoutRoot.split('/').filter { it.isNotEmpty() }
    }

    override fun getFileName(): Path? {
        val comps = getComponents()
        return if (comps.isEmpty()) null else NativePath(comps.last())
    }

    override fun getParent(): Path? {
        val comps = getComponents()
        if (comps.isEmpty()) return null
        val root = getRoot()
        if (comps.size == 1) {
            return root
        }
        val parentPath = comps.subList(0, comps.size - 1).joinToString("/")
        return if (root != null) {
            val r = root.toString()
            NativePath(if (r.endsWith("/")) "$r$parentPath" else "$r/$parentPath")
        } else {
            NativePath(parentPath)
        }
    }

    override fun getNameCount(): Int = getComponents().size

    override fun getName(index: Int): Path {
        val comps = getComponents()
        require(index in comps.indices) { "Index $index out of bounds for path ${toString()}" }
        return NativePath(comps[index])
    }

    override fun subpath(beginIndex: Int, endIndex: Int): Path {
        val comps = getComponents()
        require(beginIndex in comps.indices && endIndex in beginIndex + 1..comps.size) {
            "Invalid subpath range: $beginIndex to $endIndex (size: ${comps.size})"
        }
        return NativePath(comps.subList(beginIndex, endIndex).joinToString("/"))
    }

    override fun startsWith(other: Path): Boolean = startsWith(other.toString())

    override fun startsWith(other: String): Boolean {
        val otherPath = NativePath(other)
        if (this.isAbsolute() != otherPath.isAbsolute()) return false
        val thisComps = this.getComponents()
        val otherComps = otherPath.getComponents()
        if (otherComps.size > thisComps.size) return false
        for (i in otherComps.indices) {
            if (thisComps[i] != otherComps[i]) return false
        }
        return true
    }

    override fun endsWith(other: Path): Boolean = endsWith(other.toString())

    override fun endsWith(other: String): Boolean {
        val otherPath = NativePath(other)
        if (otherPath.isAbsolute()) {
            return this.compareTo(otherPath) == 0
        }
        val thisComps = this.getComponents()
        val otherComps = otherPath.getComponents()
        if (otherComps.size > thisComps.size) return false
        val offset = thisComps.size - otherComps.size
        for (i in otherComps.indices) {
            if (thisComps[offset + i] != otherComps[i]) return false
        }
        return true
    }

    override fun normalize(): Path {
        val comps = getComponents()
        val stack = mutableListOf<String>()
        for (comp in comps) {
            if (comp == ".") continue
            if (comp == "..") {
                if (stack.isNotEmpty() && stack.last() != "..") {
                    stack.removeAt(stack.size - 1)
                } else if (!isAbsolute()) {
                    stack.add("..")
                }
            } else {
                stack.add(comp)
            }
        }
        val joined = stack.joinToString("/")
        val root = getRoot()
        return if (root != null) {
            val r = root.toString()
            NativePath(if (r.endsWith("/")) "$r$joined" else "$r/$joined")
        } else {
            NativePath(if (joined.isEmpty()) "." else joined)
        }
    }

    override fun resolve(other: Path): Path = resolve(other.toString())

    override fun resolve(other: String): Path {
        val otherPath = NativePath(other)
        if (otherPath.isAbsolute() || other.isEmpty()) return otherPath
        val cur = normalizedPath
        val sep = if (cur.endsWith("/")) "" else "/"
        return NativePath("$cur$sep$other")
    }

    override fun resolveSibling(other: Path): Path = resolveSibling(other.toString())

    override fun resolveSibling(other: String): Path {
        val parent = getParent() ?: return NativePath(other)
        return parent.resolve(other)
    }

    override fun relativize(other: Path): Path {
        require(this.isAbsolute() == other.isAbsolute()) {
            "Cannot relativize between absolute and relative paths"
        }
        val thisComps = this.getComponents()
        val otherComps = (other as NativePath).getComponents()
        var common = 0
        while (common < thisComps.size && common < otherComps.size && thisComps[common] == otherComps[common]) {
            common++
        }
        val upCount = thisComps.size - common
        val parts = mutableListOf<String>()
        for (i in 0 until upCount) parts.add("..")
        for (i in common until otherComps.size) parts.add(otherComps[i])
        return NativePath(if (parts.isEmpty()) "" else parts.joinToString("/"))
    }

    override fun toUri(): URI {
        val abs = toAbsolutePath().toString()
        return URI("file://$abs")
    }

    override fun toAbsolutePath(): Path {
        if (isAbsolute()) return this
        val userDir = File(".").getAbsolutePath()
        return NativePath(userDir).resolve(this).normalize()
    }

    override fun toRealPath(vararg options: LinkOption): Path {
        return toAbsolutePath()
    }

    override fun toFile(): File = File(normalizedPath)

    override fun iterator(): Iterator<Path> {
        val comps = getComponents()
        val paths = comps.map { NativePath(it) }
        return paths.iterator()
    }

    override fun compareTo(other: Path): Int = this.toString().compareTo(other.toString())

    override fun toString(): String = pathString

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Path) return false
        return this.normalizedPath == other.toString().replace('\\', '/')
    }

    override fun hashCode(): Int = normalizedPath.hashCode()
}

actual class Paths {
    actual companion object {
        actual fun get(first: String, vararg more: String): Path {
            if (more.isEmpty()) return NativePath(first)
            val sb = StringBuilder(first)
            for (m in more) {
                if (sb.isNotEmpty() && !sb.endsWith("/")) sb.append('/')
                sb.append(m)
            }
            return NativePath(sb.toString())
        }

        actual fun get(uri: URI): Path {
            return NativePath(uri.path ?: "")
        }
    }
}
