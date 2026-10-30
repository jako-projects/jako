package kotlin.io

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

public enum class OnErrorAction {
    SKIP,
    TERMINATE
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.copyTo(target: File, overwrite: Boolean = false, bufferSize: Int = 8192): File {
    if (!this.exists()) {
        throw FileNotFoundException("The source file $this doesn't exist.")
    }
    if (target.exists()) {
        if (!overwrite) {
            throw IOException("The destination file $target already exists.")
        }
        if (!target.delete()) {
            throw IOException("Unable to delete existing destination file $target")
        }
    }
    if (this.isDirectory()) {
        if (!target.mkdirs()) {
            throw IOException("Unable to create directory $target")
        }
    } else {
        val parent = target.getParentFile()
        if (parent != null) {
            parent.mkdirs()
        }
        val input: InputStream = FileInputStream(this)
        try {
            val output: OutputStream = FileOutputStream(target)
            try {
                input.copyTo(output, bufferSize)
            } finally {
                output.close()
            }
        } finally {
            input.close()
        }
    }
    return target
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.deleteRecursively(): Boolean {
    val files = listFiles()
    if (files != null) {
        for (file in files) {
            if (!file.deleteRecursively()) {
                return false
            }
        }
    }
    return delete()
}

public val File.extension: String
    get() = getName().substringAfterLast('.', "")

public val File.nameWithoutExtension: String
    get() = getName().substringBeforeLast(".")

public val File.invariantSeparatorsPath: String
    get() = if (File.separatorChar != '/') getPath().replace(File.separatorChar, '/') else getPath()

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.copyRecursively(
    target: File,
    overwrite: Boolean = false,
    onError: (File, IOException) -> OnErrorAction = { _, exception -> throw exception }
): Boolean {
    if (!exists()) {
        return onError(this, NoSuchFileException(file = this, reason = "The source file doesn't exist.")) != OnErrorAction.TERMINATE
    }
    try {
        for (src in walkTopDown().onFail { f, e -> if (onError(f, e) == OnErrorAction.TERMINATE) throw TerminateException(f) }) {
            if (!src.exists()) {
                if (onError(src, NoSuchFileException(file = src, reason = "The source file doesn't exist.")) == OnErrorAction.TERMINATE) {
                    return false
                }
            } else {
                val relPath = src.toRelativeString(this)
                val dstFile = if (relPath.isEmpty()) target else File(target, relPath)
                if (dstFile.exists() && !(src.isDirectory() && dstFile.isDirectory())) {
                    val stillExists = if (!overwrite) true else {
                        if (dstFile.isDirectory()) !dstFile.deleteRecursively() else !dstFile.delete()
                    }
                    if (stillExists) {
                        if (onError(dstFile, FileAlreadyExistsException(file = src, other = dstFile, reason = "The destination file already exists.")) == OnErrorAction.TERMINATE) {
                            return false
                        }
                        continue
                    }
                }
                if (src.isDirectory()) {
                    dstFile.mkdirs()
                } else {
                    if (src.copyTo(dstFile, overwrite).length() != src.length()) {
                        if (onError(src, IOException("Source file wasn't copied completely, length of destination file differs.")) == OnErrorAction.TERMINATE) {
                            return false
                        }
                    }
                }
            }
        }
        return true
    } catch (e: TerminateException) {
        return false
    }
}

private class TerminateException(file: File) : FileSystemException(file)

private fun splitPathComponents(path: String): Pair<String, List<String>> {
    val normalized = path.replace('\\', '/')
    val root = when {
        normalized.startsWith("/") -> "/"
        normalized.length >= 2 && normalized[1] == ':' -> {
            if (normalized.length >= 3 && normalized[2] == '/') normalized.substring(0, 3) else normalized.substring(0, 2)
        }
        else -> ""
    }
    val sub = normalized.substring(root.length)
    val rawParts = if (sub.isEmpty()) emptyList() else sub.split('/').filter { it.isNotEmpty() }
    val normalizedParts = ArrayList<String>()
    for (part in rawParts) {
        when (part) {
            "." -> {}
            ".." -> {
                if (normalizedParts.isNotEmpty() && normalizedParts.last() != "..") {
                    normalizedParts.removeAt(normalizedParts.size - 1)
                } else if (root.isEmpty()) {
                    normalizedParts.add("..")
                }
            }
            else -> normalizedParts.add(part)
        }
    }
    return Pair(root, normalizedParts)
}

private fun File.toRelativeStringOrNull(base: File): String? {
    val (thisRoot, thisParts) = splitPathComponents(this.getPath())
    val (baseRoot, baseParts) = splitPathComponents(base.getPath())
    if (thisRoot != baseRoot) return null

    var common = 0
    val minSize = if (thisParts.size < baseParts.size) thisParts.size else baseParts.size
    while (common < minSize && thisParts[common] == baseParts[common]) {
        common++
    }

    for (i in common until baseParts.size) {
        if (baseParts[i] == "..") return null
    }

    val resultParts = ArrayList<String>()
    for (i in common until baseParts.size) {
        resultParts.add("..")
    }
    for (i in common until thisParts.size) {
        resultParts.add(thisParts[i])
    }
    return resultParts.joinToString(File.separator)
}

public fun File.toRelativeString(base: File): String =
    toRelativeStringOrNull(base) ?: throw IllegalArgumentException("this and base files have different roots: $this and $base.")

public fun File.relativeTo(base: File): File = File(this.toRelativeString(base))

public fun File.relativeToOrSelf(base: File): File =
    toRelativeStringOrNull(base)?.let { File(it) } ?: this

public fun File.relativeToOrNull(base: File): File? =
    toRelativeStringOrNull(base)?.let { File(it) }

public fun File.startsWith(other: File): Boolean {
    val (thisRoot, thisParts) = splitPathComponents(this.getPath())
    val (otherRoot, otherParts) = splitPathComponents(other.getPath())
    if (thisRoot != otherRoot) return false
    if (thisParts.size < otherParts.size) return false
    for (i in otherParts.indices) {
        if (thisParts[i] != otherParts[i]) return false
    }
    return true
}

public fun File.startsWith(other: String): Boolean = startsWith(File(other))

public fun File.endsWith(other: File): Boolean {
    val (thisRoot, thisParts) = splitPathComponents(this.getPath())
    val (otherRoot, otherParts) = splitPathComponents(other.getPath())
    if (otherRoot.isNotEmpty()) {
        return thisRoot == otherRoot && thisParts == otherParts
    }
    if (thisParts.size < otherParts.size) return false
    val offset = thisParts.size - otherParts.size
    for (i in otherParts.indices) {
        if (thisParts[offset + i] != otherParts[i]) return false
    }
    return true
}

public fun File.endsWith(other: String): Boolean = endsWith(File(other))
