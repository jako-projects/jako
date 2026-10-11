package kotlin.io

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.io.StringWriter
import java.nio.charset.Charset
import kotlin.text.Charsets

public fun File.readText(charset: Charset = Charsets.UTF_8): String = readText(charset.name())

public fun File.readText(charsetName: String): String {
    val r: Reader = FileInputStream(this).reader(charsetName)
    try {
        return r.readText()
    } finally {
        r.close()
    }
}

public fun File.writeText(text: String, charset: Charset = Charsets.UTF_8) {
    writeText(text, charset.name())
}

public fun File.writeText(text: String, charsetName: String) {
    val w: Writer = FileOutputStream(this).writer(charsetName)
    try {
        w.write(text)
    } finally {
        w.close()
    }
}

public fun File.appendText(text: String, charset: Charset = Charsets.UTF_8) {
    appendText(text, charset.name())
}

public fun File.appendText(text: String, charsetName: String) {
    val w: Writer = FileOutputStream(this, true).writer(charsetName)
    try {
        w.write(text)
    } finally {
        w.close()
    }
}

public fun File.readBytes(): ByteArray {
    val s: InputStream = FileInputStream(this)
    try {
        return s.readBytes()
    } finally {
        s.close()
    }
}

public fun File.writeBytes(array: ByteArray) {
    val s: OutputStream = FileOutputStream(this)
    try {
        s.write(array)
    } finally {
        s.close()
    }
}

public fun File.appendBytes(array: ByteArray) {
    val s: OutputStream = FileOutputStream(this, true)
    try {
        s.write(array)
    } finally {
        s.close()
    }
}

public fun File.forEachLine(charset: Charset = Charsets.UTF_8, action: (String) -> Unit) {
    forEachLine(charset.name(), action)
}

public fun File.forEachLine(charsetName: String, action: (String) -> Unit) {
    val r: Reader = FileInputStream(this).reader(charsetName)
    try {
        r.forEachLine(action)
    } finally {
        r.close()
    }
}

public fun File.readLines(charset: Charset = Charsets.UTF_8): List<String> = readLines(charset.name())

public fun File.readLines(charsetName: String): List<String> {
    val r: Reader = FileInputStream(this).reader(charsetName)
    try {
        return r.readLines()
    } finally {
        r.close()
    }
}

public fun <T> File.useLines(charset: Charset = Charsets.UTF_8, block: (Sequence<String>) -> T): T =
    useLines(charset.name(), block)

public fun <T> File.useLines(charsetName: String, block: (Sequence<String>) -> T): T {
    val r: Reader = FileInputStream(this).reader(charsetName)
    try {
        return r.useLines(block)
    } finally {
        r.close()
    }
}

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

public fun File.bufferedReader(charset: Charset = Charsets.UTF_8, bufferSize: Int = 8192): BufferedReader =
    reader(charset).buffered(bufferSize)

public fun File.bufferedWriter(charset: Charset = Charsets.UTF_8, bufferSize: Int = 8192): BufferedWriter =
    writer(charset).buffered(bufferSize)

public fun File.reader(charset: Charset = Charsets.UTF_8): InputStreamReader =
    FileInputStream(this).reader(charset)

public fun File.writer(charset: Charset = Charsets.UTF_8): OutputStreamWriter =
    FileOutputStream(this).writer(charset)

public enum class FileWalkDirection {
    TOP_DOWN,
    BOTTOM_UP
}

public enum class OnErrorAction {
    SKIP,
    TERMINATE
}

public open class FileSystemException(
    public val file: File,
    public val other: File? = null,
    public val reason: String? = null
) : IOException(buildMessage(file, other, reason))

private fun buildMessage(file: File, other: File?, reason: String?): String {
    val sb = StringBuilder(file.toString())
    if (other != null) {
        sb.append(" -> $other")
    }
    if (reason != null) {
        sb.append(": $reason")
    }
    return sb.toString()
}

public class FileAlreadyExistsException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)

public class NoSuchFileException(
    file: File,
    other: File? = null,
    reason: String? = null
) : FileSystemException(file, other, reason)

public class FileTreeWalk private constructor(
    private val start: File,
    private val direction: FileWalkDirection = FileWalkDirection.TOP_DOWN,
    private val onEnter: ((File) -> Boolean)?,
    private val onLeave: ((File) -> Unit)?,
    private val onFail: ((File, IOException) -> Unit)?,
    private val maxDepth: Int = Int.MAX_VALUE
) : Sequence<File> {

    internal constructor(start: File, direction: FileWalkDirection = FileWalkDirection.TOP_DOWN) : this(
        start, direction, null, null, null
    )

    public override fun iterator(): Iterator<File> = FileTreeWalkIterator()

    public fun onEnter(function: (File) -> Boolean): FileTreeWalk =
        FileTreeWalk(start, direction, onEnter = function, onLeave = onLeave, onFail = onFail, maxDepth = maxDepth)

    public fun onLeave(function: (File) -> Unit): FileTreeWalk =
        FileTreeWalk(start, direction, onEnter = onEnter, onLeave = function, onFail = onFail, maxDepth = maxDepth)

    public fun onFail(function: (File, IOException) -> Unit): FileTreeWalk =
        FileTreeWalk(start, direction, onEnter = onEnter, onLeave = onLeave, onFail = function, maxDepth = maxDepth)

    public fun maxDepth(depth: Int): FileTreeWalk {
        if (depth <= 0) {
            throw IllegalArgumentException("depth must be positive, but was $depth.")
        }
        return FileTreeWalk(start, direction, onEnter, onLeave, onFail, depth)
    }

    private inner class FileTreeWalkIterator : Iterator<File> {
        private val results = ArrayList<File>()
        private var index = 0

        init {
            if (start.exists()) {
                walkRecursive(start, 1)
            }
        }

        private fun walkRecursive(current: File, depth: Int) {
            if (!current.isDirectory()) {
                results.add(current)
                return
            }
            if (onEnter != null && !onEnter.invoke(current)) {
                return
            }
            if (direction == FileWalkDirection.TOP_DOWN) {
                results.add(current)
            }
            if (depth < maxDepth) {
                val children = current.listFiles()
                if (children == null) {
                    onFail?.invoke(current, IOException("Cannot list files in a directory"))
                } else {
                    for (child in children) {
                        walkRecursive(child, depth + 1)
                    }
                }
            }
            if (direction == FileWalkDirection.BOTTOM_UP) {
                results.add(current)
            }
            onLeave?.invoke(current)
        }

        override fun hasNext(): Boolean = index < results.size

        override fun next(): File {
            if (!hasNext()) throw NoSuchElementException()
            return results[index++]
        }
    }
}

public fun File.walk(direction: FileWalkDirection = FileWalkDirection.TOP_DOWN): FileTreeWalk =
    FileTreeWalk(this, direction)

public fun File.walkTopDown(): FileTreeWalk = walk(FileWalkDirection.TOP_DOWN)

public fun File.walkBottomUp(): FileTreeWalk = walk(FileWalkDirection.BOTTOM_UP)

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

// --- InputStream extensions ---

public fun InputStream.readBytes(): ByteArray {
    val out = java.io.ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytes = read(buffer)
    }
    return out.toByteArray()
}

public fun InputStream.copyTo(out: OutputStream, bufferSize: Int = 8192): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
    }
    return bytesCopied
}

public fun InputStream.bufferedReader(charset: Charset = Charsets.UTF_8): BufferedReader =
    bufferedReader(charset.name())

public fun InputStream.bufferedReader(charsetName: String): BufferedReader =
    BufferedReader(InputStreamReader(this, charsetName))

public fun InputStream.reader(charset: Charset = Charsets.UTF_8): InputStreamReader =
    reader(charset.name())

public fun InputStream.reader(charsetName: String): InputStreamReader =
    InputStreamReader(this, charsetName)

public fun InputStream.buffered(bufferSize: Int = 8192): java.io.BufferedInputStream =
    if (this is java.io.BufferedInputStream) this else java.io.BufferedInputStream(this, bufferSize)

// --- OutputStream extensions ---

public fun OutputStream.bufferedWriter(charset: Charset = Charsets.UTF_8): BufferedWriter =
    bufferedWriter(charset.name())

public fun OutputStream.bufferedWriter(charsetName: String): BufferedWriter =
    BufferedWriter(OutputStreamWriter(this, charsetName))

public fun OutputStream.writer(charset: Charset = Charsets.UTF_8): OutputStreamWriter =
    writer(charset.name())

public fun OutputStream.writer(charsetName: String): OutputStreamWriter =
    OutputStreamWriter(this, charsetName)

public fun OutputStream.buffered(bufferSize: Int = 8192): java.io.BufferedOutputStream =
    if (this is java.io.BufferedOutputStream) this else java.io.BufferedOutputStream(this, bufferSize)

// --- Reader extensions ---

public fun Reader.readText(): String {
    val buffer = StringWriter()
    copyTo(buffer)
    return buffer.toString()
}

public fun Reader.readLines(): List<String> {
    val result = ArrayList<String>()
    forEachLine { result.add(it) }
    return result
}

public fun Reader.copyTo(out: Writer, bufferSize: Int = 8192): Long {
    var charsCopied: Long = 0
    val buffer = CharArray(bufferSize)
    var chars = read(buffer)
    while (chars >= 0) {
        out.write(buffer, 0, chars)
        charsCopied += chars
        chars = read(buffer)
    }
    return charsCopied
}

public fun Reader.forEachLine(action: (String) -> Unit) {
    buffered().lineSequence().forEach(action)
}

public fun <T> Reader.useLines(block: (Sequence<String>) -> T): T {
    val r = buffered()
    try {
        return block(r.lineSequence())
    } finally {
        r.close()
    }
}

public fun Reader.buffered(bufferSize: Int = 8192): BufferedReader =
    if (this is BufferedReader) this else BufferedReader(this, bufferSize)

public fun BufferedReader.lineSequence(): Sequence<String> = Sequence {
    object : Iterator<String> {
        private var nextValue: String? = null
        private var done = false

        override fun hasNext(): Boolean {
            if (nextValue == null && !done) {
                nextValue = readLine()
                if (nextValue == null) done = true
            }
            return nextValue != null
        }

        override fun next(): String {
            if (!hasNext()) throw NoSuchElementException()
            val answer = nextValue
            nextValue = null
            return answer!!
        }
    }
}

public fun BufferedReader.forEachLine(action: (String) -> Unit) {
    lineSequence().forEach(action)
}

public fun Writer.buffered(bufferSize: Int = 8192): BufferedWriter =
    if (this is BufferedWriter) this else BufferedWriter(this, bufferSize)
