package java.nio.file

import java.io.IOException
import java.nio.file.attribute.BasicFileAttributes
import java.nio.file.attribute.nativeReadBasicAttributes

actual enum class FileVisitResult {
    CONTINUE,
    TERMINATE,
    SKIP_SUBTREE,
    SKIP_SIBLINGS
}

actual enum class FileVisitOption {
    FOLLOW_LINKS
}

actual interface FileVisitor<T> {
    actual fun preVisitDirectory(dir: T, attrs: BasicFileAttributes): FileVisitResult
    actual fun visitFile(file: T, attrs: BasicFileAttributes): FileVisitResult
    actual fun visitFileFailed(file: T, exc: IOException): FileVisitResult
    actual fun postVisitDirectory(dir: T, exc: IOException?): FileVisitResult
}

actual open class SimpleFileVisitor<T> actual protected constructor() : FileVisitor<T> {
    actual override fun preVisitDirectory(dir: T, attrs: BasicFileAttributes): FileVisitResult =
        FileVisitResult.CONTINUE

    actual override fun visitFile(file: T, attrs: BasicFileAttributes): FileVisitResult =
        FileVisitResult.CONTINUE

    actual override fun visitFileFailed(file: T, exc: IOException): FileVisitResult {
        throw exc
    }

    actual override fun postVisitDirectory(dir: T, exc: IOException?): FileVisitResult {
        if (exc != null) throw exc
        return FileVisitResult.CONTINUE
    }
}

internal fun walkFileTreeImpl(
    start: Path,
    maxDepth: Int,
    visitor: FileVisitor<in Path>
): Path {
    if (maxDepth < 0) throw IllegalArgumentException("'maxDepth' is negative")
    walk(start, 0, maxDepth, visitor)
    return start
}

private fun walk(
    path: Path,
    depth: Int,
    maxDepth: Int,
    visitor: FileVisitor<in Path>
): FileVisitResult {
    val attrs = try {
        nativeReadBasicAttributes(path)
    } catch (e: IOException) {
        return visitor.visitFileFailed(path, e)
    }

    if (!attrs.isDirectory()) {
        return visitor.visitFile(path, attrs)
    }

    val pre = visitor.preVisitDirectory(path, attrs)
    when (pre) {
        FileVisitResult.TERMINATE -> return FileVisitResult.TERMINATE
        FileVisitResult.SKIP_SIBLINGS -> {
            val post = visitor.postVisitDirectory(path, null)
            return if (post == FileVisitResult.TERMINATE) FileVisitResult.TERMINATE else FileVisitResult.SKIP_SIBLINGS
        }
        FileVisitResult.SKIP_SUBTREE -> {
            return normalizePost(visitor.postVisitDirectory(path, null))
        }
        FileVisitResult.CONTINUE -> { /* walk children when depth allows */ }
    }

    var ioExc: IOException? = null
    if (depth < maxDepth) {
        val files = path.toFile().listFiles()
        if (files == null) {
            ioExc = IOException("Unable to list directory $path")
        } else {
            for (childFile in files) {
                val child = path.resolve(childFile.getName())
                val result = walk(child, depth + 1, maxDepth, visitor)
                if (result == FileVisitResult.TERMINATE) return FileVisitResult.TERMINATE
                if (result == FileVisitResult.SKIP_SIBLINGS) break
            }
        }
    }

    return normalizePost(visitor.postVisitDirectory(path, ioExc))
}

private fun normalizePost(result: FileVisitResult): FileVisitResult {
    return when (result) {
        FileVisitResult.SKIP_SUBTREE -> FileVisitResult.CONTINUE
        else -> result
    }
}
