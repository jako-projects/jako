package kotlin.io.path

import java.nio.file.LinkOption
import java.nio.file.Path

public operator fun Path.div(other: String): Path = resolve(other)
public operator fun Path.div(other: Path): Path = resolve(other)

public fun Path.exists(vararg options: LinkOption): Boolean = toFile().exists()

public fun Path.notExists(vararg options: LinkOption): Boolean = !toFile().exists()

public fun Path.isDirectory(vararg options: LinkOption): Boolean = toFile().isDirectory()

public fun Path.isRegularFile(vararg options: LinkOption): Boolean = toFile().isFile()

public val Path.extension: String
    get() {
        val fileName = getFileName()?.toString() ?: return ""
        val idx = fileName.lastIndexOf('.')
        return if (idx == -1 || idx == fileName.length - 1) "" else fileName.substring(idx + 1)
    }

public val Path.nameWithoutExtension: String
    get() {
        val fileName = getFileName()?.toString() ?: return ""
        val idx = fileName.lastIndexOf('.')
        return if (idx == -1) fileName else fileName.substring(0, idx)
    }
